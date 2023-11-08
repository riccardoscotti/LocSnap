minikube start --mount --mount-string="/home/christian/Universita/Magistrale/ContextAware/Esame/LocSnap/:/cas/";
minikube ssh 'cd /cas/backend; docker build -t localbackend .';
minikube ssh 'cd /cas/frontend; docker build -t localfrontend .';
eval $(minikube docker-env);
kubectl create -f ./locsnap-deployment.yml;
sleep 15;
wezterm start -- kubectl port-forward deployment/locsnap-deployment 5432:5432;
wezterm start -- kubectl port-forward deployment/locsnap-deployment 8080:8080;
wezterm start -- kubectl port-forward deployment/locsnap-deployment 3000:3000;
