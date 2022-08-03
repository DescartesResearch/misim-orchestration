import json
import os.path
from typing import Optional
from waiting import wait
import uvicorn
from fastapi import FastAPI, Request
from fastapi.responses import FileResponse
from pydantic import BaseModel
import codecs


class Payload(BaseModel):
    data: str = '{"data":"Nothing in here yet"}'
    numberPendingPods: int = 0
    deletedPods: str = '{"data":"Nothing in here yet"}'


app = FastAPI()
# https://stackoverflow.com/questions/63949240/python-global-variable-in-fastapi-not-working-as-normal
app.type = "00"


@app.get("/")
def read_root():
    return {"Hello": "World"}


path = "./yaml-files"
path_specific = "./specific-files"

### Necessary Endpoints for the scheduler to GET mock information ###

# http://127.0.0.1:8001/apis/apps/v1/replicasets?limit=500&resourceVersion=0
@app.get("/apis/apps/v1/replicasets")
def read_item(limit: Optional[int] = None, resourceVersion: Optional[str] = None):
    file_path = os.path.join(path_specific, "ReplicaSetList.json")
    if os.path.exists(file_path):
        return FileResponse(file_path)
    return {"error": "File not found!"}


# http://127.0.0.1:8001/api/v1/persistentvolumes?limit=500&resourceVersion=0
@app.get("/api/v1/persistentvolumes")
def read_item(limit: Optional[int] = None, resourceVersion: Optional[str] = None):
    file_path = os.path.join(path, "PersistentVolume.json")
    if os.path.exists(file_path):
        return FileResponse(file_path)
    return {"error": "File not found!"}


# http://127.0.0.1:8001/api/v1/namespaces?limit=500&resourceVersion=0
@app.get("/api/v1/namespaces")
def read_item(limit: Optional[int] = None, resourceVersion: Optional[str] = None):
    file_path = os.path.join(path, "namespaceList.json")
    if os.path.exists(file_path):
        return FileResponse(file_path)
    return {"error": "File not found!"}


# http://127.0.0.1:8001/apis/apps/v1/statefulsets?limit=500&resourceVersion=0
@app.get("/apis/apps/v1/statefulsets")
def read_item(limit: Optional[int] = None, resourceVersion: Optional[str] = None):
    file_path = os.path.join(path, "StatefulSetList.json")
    if os.path.exists(file_path):
        return FileResponse(file_path)
    return {"error": "File not found!"}


# http://127.0.0.1:8001/apis/storage.k8s.io/v1/storageclasses?limit=500&resourceVersion=0
@app.get("/apis/storage.k8s.io/v1/storageclasses")
def read_item(limit: Optional[int] = None, resourceVersion: Optional[str] = None):
    file_path = os.path.join(path, "storageclasses.json")
    if os.path.exists(file_path):
        return FileResponse(file_path)
    return {"error": "File not found!"}


# http://127.0.0.1:8001/apis/storage.k8s.io/v1/csidrivers?limit=500&resourceVersion=0
@app.get("/apis/storage.k8s.io/v1/csidrivers")
def read_item(limit: Optional[int] = None, resourceVersion: Optional[str] = None):
    file_path = os.path.join(path, "csidrivers.json")
    if os.path.exists(file_path):
        return FileResponse(file_path)
    return {"error": "File not found!"}


# http://127.0.0.1:8001/apis/policy/v1/poddisruptionbudgets?limit=500&resourceVersion=0
@app.get("/apis/policy/v1/poddisruptionbudgets")
def read_item(limit: Optional[int] = None, resourceVersion: Optional[str] = None):
    file_path = os.path.join(path, "poddisruptionbudgets.json")
    if os.path.exists(file_path):
        return FileResponse(file_path)
    return {"error": "File not found!"}


# http://127.0.0.1:8001/apis/storage.k8s.io/v1/csinodes?limit=500&resourceVersion=0
@app.get("/apis/storage.k8s.io/v1/csinodes")
def read_item(limit: Optional[int] = None, resourceVersion: Optional[str] = None):
    file_path = os.path.join(path, "csinodes.json")
    if os.path.exists(file_path):
        return FileResponse(file_path)
    return {"error": "File not found!"}


# http://127.0.0.1:8001/api/v1/persistentvolumeclaims?limit=500&resourceVersion=0
@app.get("/api/v1/persistentvolumeclaims")
def read_item(limit: Optional[int] = None, resourceVersion: Optional[str] = None):
    file_path = os.path.join(path, "persistentvolumeclaims.json")
    if os.path.exists(file_path):
        return FileResponse(file_path)
    return {"error": "File not found!"}


# http://127.0.0.1:8001/apis/storage.k8s.io/v1beta1/csistoragecapacities?limit=500&resourceVersion=0
@app.get("/apis/storage.k8s.io/v1beta1/csistoragecapacities")
def read_item(limit: Optional[int] = None, resourceVersion: Optional[str] = None):
    file_path = os.path.join(path, "csistoragecapacities.json")
    if os.path.exists(file_path):
        return FileResponse(file_path)
    return {"error": "File not found!"}


# http://127.0.0.1:8001/api/v1/services?limit=500&resourceVersion=0
@app.get("/api/v1/services")
def read_item(limit: Optional[int] = None, resourceVersion: Optional[str] = None):
    file_path = os.path.join(path, "services.json")
    if os.path.exists(file_path):
        return FileResponse(file_path)
    return {"error": "File not found!"}


# http://127.0.0.1:8001/api/v1/replicationcontrollers?limit=500&resourceVersion=0
@app.get("/api/v1/replicationcontrollers")
def read_item(limit: Optional[int] = None, resourceVersion: Optional[str] = None):
    file_path = os.path.join(path, "replicationcontrollers.json")
    if os.path.exists(file_path):
        return FileResponse(file_path)
    return {"error": "File not found!"}


# http://127.0.0.1:8001/apis/events.k8s.io/v1
@app.get("/apis/events.k8s.io/v1")
def read_item():
    print("reading events \n\n\n")
    file_path = os.path.join(path, "events.json")
    if os.path.exists(file_path):
        return FileResponse(file_path)
    return {"error": "File not found!"}


# http://127.0.0.1:8001/apis/events.k8s.io/v1/namespaces/default/events/
@app.get("/apis/events.k8s.io/v1/namespaces/default/events/")
def read_item():
    print("reading events with created things \n\n\n")
    file_path = os.path.join(path, "allevents.json")
    if os.path.exists(file_path):
        return FileResponse(file_path)
    return {"error": "File not found!"}


# http://127.0.0.1:8001/apis/apps/
@app.get("/apis/apps")
def read_item():
    file_path = os.path.join(path, "apps.json")
    if os.path.exists(file_path):
        return FileResponse(file_path)
    return {"error": "File not found!"}


# http://127.0.0.1:8001/apis/coordination.k8s.io/v1/namespaces/kube-system/leases/kube-scheduler?timeout=5s
@app.get("/apis/coordination.k8s.io/v1/namespaces/kube-system/leases/kube-scheduler")
def read_item(timeout: Optional[str] = None):
    file_path = os.path.join(path, "kube-scheduler.json")
    if os.path.exists(file_path):
        return FileResponse(file_path)
    return {"error": "File not found!"}


# http://127.0.0.1:8001/apis/coordination.k8s.io/v1/namespaces/kube-system/leases/kube-scheduler?timeout=5s
@app.put("/apis/coordination.k8s.io/v1/namespaces/kube-system/leases/kube-scheduler")
async def update_item(request: Request):
    print("putting lease kube-scheduler")
    # file_path = os.path.join(path, "kube-scheduler.json")
    # if os.path.exists(file_path):
    #     return FileResponse(file_path)
    return {"SimulatedLeaderElectionOk": "You put nothing in here. You only called this method"}


# http://127.0.0.1:8000/api/v1/namespaces/default/events
@app.post("/api/v1/namespaces/default/events")
async def update_item(request: Request):
    print("reading namespaces / Events")
    # file_path = os.path.join(path, "kube-scheduler.json")
    # if os.path.exists(file_path):
    #     return FileResponse(file_path)
    return {"SimulatedPostEvent": "You posted nothing in here. You only called this method"}


# http://127.0.0.1:8001/apis/events.k8s.io/v1/namespaces/default/events
@app.post("/apis/events.k8s.io/v1/namespaces/default/events")
async def update_item(request: Request):
    print("posting events")
    # file_path = os.path.join(path, "kube-scheduler.json")
    # if os.path.exists(file_path):
    #     return FileResponse(file_path)
    return {"SimulatedPostEvent": "You posted nothing in here. You only called this method"}


# http://127.0.0.1:8000/apis/events.k8s.io/v1/namespaces/default/events/podname
@app.patch("/apis/events.k8s.io/v1/namespaces/default/events/{pod_name}")
async def update_item(pod_name: str, request: Request):
    print("patch")
    content = await request.body()
    decoded = (content.decode())
    print(decoded)
    return {"faked": "patch event for" + str(pod_name)}


### Relevant Endpoints that communicate with MiSim ###

import time

app.queried_node_list = False
app.hasNodes = False
app.didChangesOnPods = False
app.didChangesOnNodes = False
app.bindList = []
app.failedList = []


@app.on_event("startup")
async def startup_event():
    file_path = os.path.join(path, "ManipulateWatchStream.json")
    text_file = open(file_path, 'w')
    text_file.write('{"data":"Nothing in here yet"}')
    text_file.close()

    file_path = os.path.join(path, "ManipulateNodeWatchStream.json")
    text_file = open(file_path, 'w')
    text_file.write('{"data":"Nothing in here yet"}')
    text_file.close()


@app.get("/api/v1/pods")
def read_item(fieldSelector: Optional[str] = None, limit: Optional[int] = None, resourceVersion: Optional[str] = None,
              watch: Optional[bool] = None):
    if watch:
        wait(lambda: app.didChangesOnPods, timeout_seconds=300)
        print("Querying for watchstream\n\n###############")
        file_path = os.path.join(path, "ManipulateWatchStream.json")
        app.didChangesOnPods = False
        return FileResponse(file_path)
    else:
        print("Querying for PodListOnly\n\n###############")
        return Payload()


@app.get("/api/v1/nodes")
def read_item(q: Optional[str] = None, watch: Optional[bool] = None):
    if watch:
        wait(lambda: app.didChangesOnNodes, timeout_seconds=300)
        print("Querying for  NODE watchstream\n\n###############")
        file_path = os.path.join(path, "ManipulateNodeWatchStream.json")
        if app.didChangesOnNodes:
            app.queried_node_list = True
            app.didChangesOnNodes = False
        return FileResponse(file_path)
    return {"data": "Nothing in here yet"}


@app.post("/updateNodes")
def update_item(payload: Payload):
    file_path = os.path.join(path, "ManipulateNodeWatchStream.json")
    text_file = open(file_path, 'w')
    text_file.write(payload.data)
    text_file.close()
    app.didChangesOnNodes = True
    return {"Updated " + "NodeList" + " with": payload}


@app.post("/update/{k8list}")
def update_item(k8list: str, payload: Payload):
    wait(lambda: app.queried_node_list, timeout_seconds=15)
    app.bindList = []
    app.failedList = []
    file_path = os.path.join(path, "ManipulateWatchStream.json")
    text_file = open(file_path, 'w')
    text_file.write(payload.data)
    text_file.close()
    app.didChangesOnPods = True

    wait(lambda: len(app.bindList) + len(app.failedList) == payload.numberPendingPods, timeout_seconds=120)
    print("Bindlist " + str(len(app.bindList)))
    print("FailedList " + str(len(app.failedList)))

    failedListNames = []

    for entry in app.failedList:
        failedListNames.append(entry["podName"])

    deletedPodsDict = json.loads(payload.deletedPods)

    deletedString = ""
    for failedPodName in failedListNames:
        deletedString += deletedPodsDict[failedPodName]

    if deletedString != "":
        file_path = os.path.join(path, "ManipulateWatchStream.json")
        text_file = open(file_path, 'w')
        text_file.write(deletedString)
        text_file.close()
        app.didChangesOnPods = True

    wait(lambda: not app.didChangesOnPods, timeout_seconds=120)

    x = {
        "bindingList": app.bindList,
        "failedList": app.failedList
    }

    return x


# http://127.0.0.1:8000/api/v1/namespaces/default/pods/mongodb-deployment-58789ffbb-kfqdj2/status
@app.patch("/api/v1/namespaces/default/pods/{pod_name}/status")
async def update_item(pod_name: str, request: Request):
    print("failed to bind pod " + pod_name + "\n\n")
    content = await request.body()
    decoded = (content.decode())
    print(decoded)

    failedInfo = {
        "podName": pod_name,
        "status": json.loads(decoded)["status"]["conditions"][0]["message"]
    }

    skip = False
    for entry in app.failedList:
        if entry["podName"] == pod_name:
            print("Already in failed list. Do not add pod " + pod_name + " again")
            return {"status": failedInfo}

    app.failedList.append(failedInfo)

    return {"status": failedInfo}


# http://127.0.0.1:8000/api/v1/namespaces/default/pods/mongodb-deployment-58789ffbb-kfqdj2/binding
@app.post("/api/v1/namespaces/default/pods/{pod_name}/binding")
async def update_item(pod_name: str, request: Request):
    print("binding pod" + pod_name + "\n\n")
    print("content" + str(request))
    decoded = None
    try:
        content = await request.body()
        decoded = (content.decode(errors="ignore"))
    except Exception:
        print("content" + str(request))

    try:
        bound_node = decoded.split("")[1].split("\"")[0]
    except Exception:
        bound_node = decoded.split("Node")[1][4:].split("\"")[0]

    binding = {
        "podName": pod_name,
        "boundNode": bound_node
    }
    app.bindList.append(binding)

    # convert into JSON:
    y = json.dumps(binding)

    return y


if __name__ == "__main__":
    uvicorn.run(app, host='0.0.0.0', port=8000)
