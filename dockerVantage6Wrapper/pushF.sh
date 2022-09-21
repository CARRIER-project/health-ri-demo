IMAGE=harbor.carrier-mu.src.surf-hosted.nl/carrier/health_ri_demo

docker build --build-arg SSH_PRIVATE_KEY="$(cat C:\\Florian\\GIT\\KEY\\id_rsa)" \
  --build-arg SSH-PUBLIC-KEY="$(cat C:\\Florian\\GIT\\KEY\\id_rsa.pub)" \
  -t health_ri_demo  .

docker tag health_ri_demo $IMAGE

docker push $IMAGE