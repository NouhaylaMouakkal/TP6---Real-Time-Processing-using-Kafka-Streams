import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.StreamsBuilder;
import org.apache.kafka.streams.StreamsConfig;
import org.apache.kafka.streams.kstream.*;

import java.util.Properties;

public class WeatherDataProcessor {
    public static void main(String[] args) {
        // Configuration de Kafka Streams
        Properties props = new Properties();
        props.put(StreamsConfig.APPLICATION_ID_CONFIG, "weather-data-processor");
        props.put(StreamsConfig.BOOTSTRAP_SERVERS_CONFIG, "localhost:9092");
        props.put(StreamsConfig.DEFAULT_KEY_SERDE_CLASS_CONFIG, Serdes.String().getClass());
        props.put(StreamsConfig.DEFAULT_VALUE_SERDE_CLASS_CONFIG, Serdes.String().getClass());

        StreamsBuilder builder = new StreamsBuilder();

        // Lecture des données depuis le topic 'weather-data'
        KStream<String, String> weatherDataStream = builder.stream("weather-data");

        // Filtrer les températures > 30°C
        KStream<String, String> highTempStream = weatherDataStream.filter((key, value) -> {
            String[] parts = value.split(",");
            double temperature = Double.parseDouble(parts[1]);
            return temperature > 30;
        });

        // Convertir les températures en Fahrenheit
        KStream<String, String> fahrenheitStream = highTempStream.mapValues(value -> {
            String[] parts = value.split(",");
            double celsius = Double.parseDouble(parts[1]);
            double fahrenheit = (celsius * 9 / 5) + 32;
            return parts[0] + "," + fahrenheit + "," + parts[2];
        });

        // Grouper par station et calculer les moyennes
        KGroupedStream<String, String> groupedByStation = fahrenheitStream.groupBy(
                (key, value) -> value.split(",")[0],
                Grouped.with(Serdes.String(), Serdes.String())
        );

        KTable<String, String> stationAverages = groupedByStation.aggregate(
                () -> "0,0,0",
                (station, newValue, aggregate) -> {
                    String[] newParts = newValue.split(",");
                    String[] aggParts = aggregate.split(",");

                    double newTemp = Double.parseDouble(newParts[1]);
                    double newHumidity = Double.parseDouble(newParts[2]);
                    double tempSum = Double.parseDouble(aggParts[0]) + newTemp;
                    double humiditySum = Double.parseDouble(aggParts[1]) + newHumidity;
                    int count = Integer.parseInt(aggParts[2]) + 1;

                    return tempSum + "," + humiditySum + "," + count;
                },
                Materialized.with(Serdes.String(), Serdes.String())
        ).mapValues(value -> {
            String[] parts = value.split(",");
            double tempAvg = Double.parseDouble(parts[0]) / Integer.parseInt(parts[2]);
            double humidityAvg = Double.parseDouble(parts[1]) / Integer.parseInt(parts[2]);
            return "Température Moyenne = " + tempAvg + "°F, Humidité Moyenne = " + humidityAvg + "%";
        });

        // Publier dans le topic 'station-averages'
        stationAverages.toStream().to("station-averages", Produced.with(Serdes.String(), Serdes.String()));

        // Construire et démarrer l'application
        KafkaStreams streams = new KafkaStreams(builder.build(), props);
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        streams.start();
    }
}
