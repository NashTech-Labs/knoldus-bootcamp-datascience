ip=192.168.1.5
main_dir=$HOME/dev/projects/TrainingSprints/datascience-bootcamp/LendingClubMl_2
resources_dir=$main_dir/data

jar=$main_dir/target/scala-2.11/lendingclubml_2.11-0.1.jar


csv=$resources_dir/loan_removed_rows.csv

log=$resources_dir/GetCorrelations.txt

#mode=cluster
mode=client


../bin/spark-submit --class com.knoldus.training.GetCorrelations --master spark://$ip:7077 --driver-memory 7G --executor-memory 4G --deploy-mode $mode --jars $aardpfark,$hadrian $jar $csv >& $log

