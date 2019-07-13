#bin=$HOME/dev/software/mesos-1.8.0/build/bin/mesos-agent.sh
bin=mesos-agent

ip=`cat IpAddress.txt`

#sudo $bin --master=192.168.1.5:5050 --work_dir=$HOME/mesos_work_dir \
	#containerizers=mesos \
	#--image_providers=docker
	#--isolation=filesystem/linux,docker/runtime

#sudo mkdir -p /tmp/mesos/store/docker

#$bin --work_dir=$HOME/mesos_work_dir \
#sudo $bin --work_dir=$HOME/mesos_work_dir \
#	--master=zk://192.168.1.5:2181/mesos \
#	--containerizers=docker,mesos \
#	--executor_registration_timeout=5mins \
#	--executor_shutdown_grace_period=60secs \
#	--hadoop_home=/home/jouko/dev/software/hadoop-3.1.2 \
#	--isolation=docker/runtime,filesystem/linux \
#	--image_providers=docker

sudo -E env "PATH=$PATH" $bin --work_dir=/var/lib/mesos/agent  --master=zk://$ip:2181/mesos --containerizers=docker,mesos --executor_registration_timeout=5mins
