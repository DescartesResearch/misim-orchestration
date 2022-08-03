== Notes ==
Requires at least python3.7
Works for kube-scheduler 1.23.x, but not for 1.24.x

== To Run ==

- Download kube-scheduler 1.23 from https://dl.k8s.io/v1.23.9/bin/linux/amd64/kube-scheduler
- Run `chmod +x kube-scheduler`
- Make sure to run at least python 3.7 on the machine
- To upgrade to python3.7 run:

```
# Tutorial: https://www.itsupportwale.com/blog/how-to-upgrade-to-python-3-7-on-ubuntu-18-10/
sudo apt-get install python3.7
sudo update-alternatives --install /usr/bin/python3 python3 /usr/bin/python3.6 1
sudo update-alternatives --install /usr/bin/python3 python3 /usr/bin/python3.7 2
sudo update-alternatives --config python3
# press 2
```

- Install dependencies using

```
python3.7 -m pip install -r requirements.txt
```

- Run the adapter `python3 main.py`
- Make sure that in the `scheduler-config.yaml` no schedulerName is set
- Run kube-scheduler `./kube-scheduler --master 127.0.0.1:8000` or `./kube-scheduler --master 127.0.0.1:8000 --config scheduler-config.yaml`