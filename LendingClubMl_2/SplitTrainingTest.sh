ip=192.168.1.5
main_dir=$HOME/dev/projects/TrainingSprints/datascience-bootcamp/LendingClubMl_2
resources_dir=$main_dir/data

jar=$main_dir/target/scala-2.11/lendingclubml_2.11-0.1.jar


csv=$resources_dir/loan.csv
test_set_size=10000
training_out_path=$resources_dir/loan_training
test_out_path=$resources_dir/loan_test

log=$resources_dir/SplitTrainingTest.txt

#mode=cluster
mode=client


../bin/spark-submit --class com.knoldus.training.SplitTrainingTest --master spark://$ip:7077 --driver-memory 7G --executor-memory 4G --deploy-mode $mode $jar $csv $test_set_size $training_out_path $test_out_path >& $log

