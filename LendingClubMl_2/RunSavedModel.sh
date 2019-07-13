ip=192.168.1.5
main_dir=$HOME/dev/projects/TrainingSprints/datascience-bootcamp/LendingClubMl_2
resources_dir=$main_dir/data

jar=$main_dir/target/scala-2.11/lendingclubml_2.11-0.1.jar


csv=$resources_dir/loan_removed_rows.csv
dt_model=$resources_dir/dt.xml
output=$resources_dir/prediction

log=$resources_dir/RunSavedModel.txt

aardpfark=$HOME/dev/software/aardpfark/target/scala-2.11/aardpfark-assembly-0.1.0-SNAPSHOT.jar
hadrian=/home/jouko/dev/software/hadrian/hadrian/target/hadrian-0.8.4.jar

#mode=cluster
mode=client

../bin/spark-submit --class com.knoldus.training.RunSavedModel --master spark://$ip:7077 --driver-memory 7G --executor-memory 4G --deploy-mode $mode $jar $csv $dt_model $output >& $log

