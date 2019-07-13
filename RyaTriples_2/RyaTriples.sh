ip=192.168.1.5
jar=target/scala-2.11/ryatriples_2.11-0.1.jar
#jar=http://$ip/ryatriples_2.11-0.1.jar


#mode=cluster
mode=client

spark_submit=$HOME/dev/software/spark-2.4.0-bin-hadoop2.7/bin/spark-submit

rdf4j_model=org.eclipse.rdf4j:rdf4j-model:2.5.0
rdf4j_rio_api=org.eclipse.rdf4j:rdf4j-rio-api:2.5.0
rdf4j_rio_turtle=org.eclipse.rdf4j:rdf4j-rio-turtle:2.5.0

#../bin/spark-submit --class com.knoldus.training.Triples --packages $rdf4j_model,$rdf4j_rio_api,$rdf4j_rio_turtle --master spark://$ip:7077 --driver-memory 10G --executor-memory 10G --jars $core_nlp,$models $jar


$spark_submit --class com.knoldus.training.GetTriples --packages $rdf4j_model,$rdf4j_rio_api,$rdf4j_rio_turtle --master spark://$ip:7077 --driver-memory 7G --executor-memory 4G --deploy-mode $mode $jar

