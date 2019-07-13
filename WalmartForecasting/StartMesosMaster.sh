#bin=$HOME/dev/software/mesos-1.8.0/build/bin/mesos-master.sh
bin=mesos-master

ip=`cat IpAddress.txt`

echo $ip

sudo -E env "PATH=$PATH" /usr/local/sbin/mesos-master --zk=zk://$ip:2181/mesos --registry=in_memory --ip=$ip --work_dir=$HOME/mesos_master_work_dir
#sudo /usr/local/sbin/mesos-master --zk=zk://192.168.1.5:2181/mesos --registry=in_memory --ip=192.168.1.5
#sudo $bin --ip=192.168.1.5 --work_dir=$HOME/mesos_work_dir
#sudo $bin --ip=192.168.1.5 --zk=zk://192.168.1.5:2181/mesos --registry=in_memory
#sudo $bin --ip=192.168.1.5 --zk=zk://192.168.1.5:2181/mesos
#$bin --ip=192.168.1.5 --zk=zk://192.168.1.5:2181/mesos --registry=in_memory
