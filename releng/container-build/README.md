Steps to deploy:

The image is tagged after the Fedora release it is based on (e.g. `f45`), and
the `Jenkinsfile` pulls that tag. When moving to a new Fedora release, update
the `FROM` line in the `Dockerfile`, use the new tag below and change the image
in the `Jenkinsfile` to it.

* Build:

```
podman build -t USER_NAME/linuxtools-build-test-dependencies:f45 .
```

* Login

```
podman login docker.io
```

* Deploy

```
podman push USER_NAME/linuxtools-build-test-dependencies:f45 docker.io/USER_NAME/linuxtools-build-test-dependencies:f45
```

Without an explicit tag, both commands use `latest`, which CI does not use.

**Note:**

USER_NAME in various configs is 'akurtakov' now. If new image is published by another user this has to be changes in various Jenkins files and configs.
