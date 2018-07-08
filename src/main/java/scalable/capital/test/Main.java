package scalable.capital.test;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.nio.charset.Charset;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Collector;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    public static void main(String[] args) {
        final Main main = new Main();

        final var optQuery = main.getQuery(args);
        final var query = optQuery.orElseThrow(IllegalArgumentException::new);

        final var optSerp = main.getResultPage(query);
        final var serp = optSerp.orElseThrow(RuntimeException::new);

        final var optUris = main.getUris(serp);
        final var uris = optUris.orElseThrow(RuntimeException::new);

        final Stream<Object> pages = uris.parallelStream()
                .map(main::getPage)
                .filter(Optional::isPresent)
                .map(Optional::get);

        final Map<String, Long> jsLibs = pages
                .flatMap(p -> main.getJsLibs(p).stream())
                .collect(Collectors.groupingBy(Function.identity(), Collectors.counting()));

        jsLibs.entrySet().stream()
                .sorted((e1, e2) -> Math.toIntExact(e1.getValue() - e2.getValue()))
                .skip(jsLibs.size() - 5)
                .map(e -> String.format("%s - %d usages", e.getKey(), e.getValue()))
                .forEachOrdered(System.out::println);
    }

    Optional<String> getQuery(String[] args) {
        return args.length > 0 ? Optional.of(args[0]) : Optional.empty();
    }

    Optional<String> getResultPage(String query) {
        return getPage("http://google.com/?q=" + URLEncoder.encode(query, Charset.defaultCharset()));
    }

    Optional<String> getPage(String uri) {
        try {
            URL url = new URL(uri);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Accept", "text/plain");

            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String inputLine;
            StringBuilder content = new StringBuilder();
            while ((inputLine = in.readLine()) != null) {
                content.append(inputLine);
            }
            in.close();

            return Optional.of(content.toString());
        } catch (IOException e) {
            e.printStackTrace();
        }
        return Optional.empty();
    }

    private List<String> getJsLibs(Object o) {
        return List.of();
    }


    private Optional<List<String>> getUris(Object serp) {
        return Optional.empty();
    }


}
