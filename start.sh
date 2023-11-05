minikube start --mount --mount-string="/home/christian/Universita/Magistrale/ContextAware/Esame/LocSnap/:/cas/";
minikube ssh 'cd /cas/backend; docker build -t localbackend .';
minikube ssh 'cd /cas/frontend; docker build -t localfrontend .'
eval $(minikube docker-env);
kubectl create -f ./k8s/database-pod.yml;
kubectl create -f ./k8s/backend-pod.yml;
kubectl create -f ./k8s/frontend-pod.yml;
sleep 10;
wezterm start -- kubectl port-forward --namespace default database-pod 5432:5432
wezterm start -- kubectl port-forward --namespace default backend-pod 8080:8080
wezterm start -- kubectl port-forward --namespace default frontend-pod 3000:3000
