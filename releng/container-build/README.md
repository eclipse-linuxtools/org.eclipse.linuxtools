Steps to deploy:

* Build:

```
podman build -t USER_NAME/linuxtools-build-test-dependencies .
```

* Login

```
podman login docker.io
```

* Deploy

```
podman push docker.io/USER_NAME/linuxtools-build-test-dependencies
```

**Note:**

USER_NAME in various configs is 'akurtakov' now. If new image is published by another user this has to be changes in various Jenkins files and configs.