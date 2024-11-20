# **TP Kafka Streams : Analyse des Données Météorologiques**

## **Description**
Ce projet met en œuvre une application Kafka Streams pour traiter des données météorologiques en temps réel. Les données brutes sont reçues depuis un topic Kafka, transformées pour filtrer les températures élevées, converties en Fahrenheit, puis agrégées par station pour calculer les moyennes de température et d'humidité. Les résultats sont publiés dans un autre topic Kafka.

## **Installation**
1. Clonez ce dépôt sur votre machine locale :
   ```bash
   git clone https://github.com/NouhaylaMouakkal/TP6-Real-Time-Processing-using-Kafka-Streams.git
   cd kafka-streams-weather
   ```

2. Configurez Kafka et Zookeeper avec Docker en créant un fichier `docker-compose.yml` :
   ```yaml
   version: '3.8'
   services:
     zookeeper:
       image: confluentinc/cp-zookeeper:7.5.0
       environment:
         ZOOKEEPER_CLIENT_PORT: 2181
       ports:
         - "2181:2181"
     kafka:
       image: confluentinc/cp-kafka:7.5.0
       depends_on:
         - zookeeper
       environment:
         KAFKA_BROKER_ID: 1
         KAFKA_ZOOKEEPER_CONNECT: zookeeper:2181
         KAFKA_ADVERTISED_LISTENERS: PLAINTEXT://localhost:9092
         KAFKA_LISTENERS: PLAINTEXT://0.0.0.0:9092
         KAFKA_OFFSETS_TOPIC_REPLICATION_FACTOR: 1
       ports:
         - "9092:9092"
   ```

3. Lancez Kafka et Zookeeper :
   ```bash
   docker-compose up -d
   ```

---

## **Création des Topics Kafka**
1. Connectez-vous au conteneur Kafka :
   ```bash
   docker exec -it <kafka_container_id> bash
   ```

2. Créez les topics nécessaires :
   - Topic pour les données brutes (`weather-data`) :
     ```bash
     kafka-topics --create --topic weather-data --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
     ```
   - Topic pour les résultats agrégés (`station-averages`) :
     ```bash
     kafka-topics --create --topic station-averages --bootstrap-server localhost:9092 --partitions 1 --replication-factor 1
     ```

---

## **Exécution de l’Application**

1. Injectez des messages dans le topic `weather-data` :
   ```bash
   kafka-console-producer --topic weather-data --bootstrap-server localhost:9092
   ```
   Exemple de données à injecter :
   ```
   Station1,25.3,60
   Station2,35.0,50
   Station2,40.0,45
   ```

2. Vérifiez les résultats dans le topic `station-averages` :
   ```bash
   kafka-console-consumer --topic station-averages --from-beginning --bootstrap-server localhost:9092
   ```

---

## **Exemple de Résultats**
### Données injectées :
```
Station1,25.3,60
Station2,35.0,50
Station2,40.0,45
```

### Résultats dans le topic `station-averages` :
```
Station2 : Température Moyenne = 99.5°F, Humidité Moyenne = 47.5%
```

---

## **Points Clés**
1. **Filtrage** : Les températures inférieures ou égales à 30 °C sont exclues.
2. **Conversion** : Les températures sont converties de Celsius à Fahrenheit.
3. **Agrégation** : Les moyennes sont calculées pour chaque station.

---

## **Dépannage**
### Problèmes Courants :
1. **Kafka ne démarre pas correctement** :
   - Vérifiez les ports 2181 (Zookeeper) et 9092 (Kafka) pour des conflits.
   - Supprimez les conteneurs et redémarrez :
     ```bash
     docker-compose down
     docker-compose up -d
     ```

2. **Pas de données dans `station-averages`** :
   - Assurez-vous que l'application Kafka Streams fonctionne et que les topics sont correctement configurés.
   - Vérifiez les logs pour des erreurs éventuelles.

---

## **Conclusion**
Ce projet illustre l'utilisation de Kafka Streams pour transformer et agréger des flux de données en temps réel. Il fournit une base solide pour développer des solutions plus complexes, comme la gestion de fenêtres temporelles ou l'intégration de plusieurs flux.

--- 

# By Nouhyla MOUAKKAL | II-BDCC3
