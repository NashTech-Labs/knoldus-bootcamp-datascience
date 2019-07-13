ip=`cat IpAddress.txt`

bin=$HOME/dev/software/spark-2.4.0-bin-hadoop2.7/sbin/start-mesos-dispatcher.sh

#$bin --zk $ip:2181 --master mesos://$ip:7077
$bin --zk $ip:2181 --master mesos://$ip:5050
#$bin --zk $ip:2181 --master mesos://zk://$ip:2181,192.168.1.3:2181/mesos
