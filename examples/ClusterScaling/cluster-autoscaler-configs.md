# Random Expansion

```
cluster-autoscaler \
    --kubeconfig kubeconfig.yaml \
    --leader-elect=false \
    --cloud-provider clusterapi \
    --scan-interval=5s
```

# Least-Waste Expansion

```
cluster-autoscaler \
    --kubeconfig kubeconfig.yaml \
    --leader-elect=false \
    --cloud-provider clusterapi \
    --scan-interval=5s \
    --expander=least-waste
```