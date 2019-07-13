ip=192.168.1.5
jar=$HOME/dev/project/TrainingSprints/datascience-bootcamp/OpenCVSpark/target/scala-2.11/opencvspark_2.11-0.1.jar
jar=http://$ip/opencvspark_2.11-0.1.jar

#open_cv=org.bytedeco:javacv:0.9
#javacpp=org.bytedeco:javacpp:0.9

mode=cluster
#mode=client

so_file=$HOME/dev/projects/TrainingSprints/datascience-bootcamp/OpenCVSpark/lib/libopencv_java2413.so
so_dir=$HOME/dev/projects/TrainingSprints/datascience-bootcamp/OpenCVSpark/lib
opencv_jar=$HOME/dev/projects/TrainingSprints/datascience-bootcamp/OpenCVSpark/lib/opencv-2413.jar

../bin/spark-submit --class CountFaces --master spark://$ip:7077 --files $so_file --conf "spark.executor.extraLibraryPath=$so_dir" --conf "spark.driver.extraLibraryPath=$so_dir" --driver-memory 7G --executor-memory 4G --deploy-mode $mode --jars $opencv_jar $jar

