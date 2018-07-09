package scalable.capital.test;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import java.io.IOException;
import java.net.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static java.util.stream.Collectors.*;

public class Main {
    private static final String USER_AGENT =
            "Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 " +
                    "(KHTML, like Gecko) Chrome/45.0.2454.101 Safari/537.36";

    public static void main(String[] args) {
        final Main main = new Main();

        final var optQuery = main.getQuery(args);
        final var query = optQuery.orElseThrow(IllegalArgumentException::new);

        final var optSerp = main.getResultPage(query);
        final var serp = optSerp.orElseThrow(RuntimeException::new);

        final var uris = main.getUris(serp);

        final Stream<Document> pages = uris.parallelStream()
                .map(main::getPage)
                .filter(Optional::isPresent)
                .map(Optional::get);

        final Map<String, Long> jsLibs = pages
                .flatMap(p -> main.getJsLibs(p).stream())
                .collect(groupingBy(Function.identity(), counting()));

        jsLibs.entrySet().stream()
                .sorted((e1, e2) -> Math.toIntExact(e1.getValue() - e2.getValue()))
                .skip(jsLibs.size() - 5)
                .map(e -> String.format("%s - %d usage(s)", e.getKey(), e.getValue()))
                .forEachOrdered(System.out::println);
    }

    Optional<String> getQuery(String[] args) {
        return args.length > 0 ? Optional.of(args[0]) : Optional.empty();
    }

    Optional<Document> getResultPage(String query) {
        final String encodedQuery = URLEncoder.encode(query, Charset.defaultCharset());
        return getPage("http://google.com/search?q=" + encodedQuery);
    }

    Optional<Document> getPage(String uri) {
        try {
            final Document document = Jsoup.connect(uri).userAgent(USER_AGENT).get();
            return Optional.of(document);
        } catch (IOException e) {
            e.printStackTrace();
            return Optional.empty();
        }
    }

    List<String> getUris(Document serp) {
        return serp.select("h3.r a").stream()
                .map(a -> a.attr("href"))
                .collect(toList());
    }

    List<String> getJsLibs(Document page) {
        return page.select("script[type=text/javascript]").stream()
                .map(a -> a.attr("src"))
                .filter(src -> src != null && !src.isEmpty())
                .collect(toList());
    }


}
