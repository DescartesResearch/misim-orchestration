# Random Expansion

```
cluster-autoscaler \
    --kubeconfig kubeconfig.yaml \
    --leader-elect=false \
    --cloud-provider clusterapi
```

# Least-Waste Expansion

```
cluster-autoscaler \
    --kubeconfig kubeconfig.yaml \
    --leader-elect=false \
    --cloud-provider clusterapi \
    --expander=least-waste
```