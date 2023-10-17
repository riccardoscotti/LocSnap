import '../css/dashboard.css';
import CollectionCard from '../components/collectionCard';
import { MapContainer, TileLayer } from "react-leaflet";
import 'leaflet/dist/leaflet.css';
import React, { useEffect, useState, createRef, useRef, useCallback } from 'react';
import axios from "axios";
import * as L from 'leaflet';
import markerIcon from '../marker-icon.png'

axios.defaults.baseURL = 'http://localhost:8080'

const header_config = {
  headers: {
    'Accept': 'application/json',
    'Content-Type': 'application/json'
  }
}

const Dashboard = () => {

  const mIcon = new L.Icon({
    iconUrl: markerIcon,
    iconRetinaUrl: markerIcon,
    iconSize: [45, 48],
    shadowSize: [50, 64],
    iconAnchor: [22, 94],
    shadowAnchor: [4, 62],
    popupAnchor: [-3, -76],
  })

  let [searchText, setSearchText] = useState("")
  let [collectionList, setCollectionList] = useState(["collezione1"])
  const searchRef = createRef()
  const mapRef = createRef()

  // Collections initialization
  useEffect(() => {
    retrieveCollections();
    loadImages();
  }, []);

  

  function useHookWithRefCallback() {
    const mapCB = useCallback(node => {
      if (node) {
        mapRef.current = node
        showMarkers()
      }
    }, [])

    return [mapCB];
  }

  function DrawMap() {
    const [mapRef] = useHookWithRefCallback()
    
    return (
      <MapContainer id='map-container' ref={mapRef} center={bolognaCoords} zoom={14} scrollWheelZoom={true} zoomControl={false} attributionControl={false}>
        <TileLayer id='tile-layer' url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
      </MapContainer>
    )

  }

  function loadImages() {
    axios.post('/retrieveimages', {
      logged_user: localStorage.getItem("user")
    })
    .then((response) => {
      if(response.data.status === 200) {
        localStorage.setItem("imgs", JSON.stringify(response.data.imgs))
      }
    })
    .catch((error) => {
        console.log(error);
    });
  }

  function showMarkers() {
    if (mapRef.current !== "undefined") {
      var markerGroup = L.layerGroup().addTo(mapRef.current);
      Object.entries(JSON.parse(localStorage.getItem("imgs"))).map( (img) => {
        var marker = new L.marker([img[1].coords[0], img[1].coords[1]], {icon: mIcon}).addTo(markerGroup);
        marker.bindPopup(img[1].name);
        console.log(img);
      })
    }
  }

  const retrieveCollections = () => {
    axios.post("/retrievecollections", {
      logged_user: localStorage.getItem("user")
    },
    header_config  
    )
    .then((response) => {
      if(response.status === 200) {
        localStorage.setItem("collections", JSON.stringify(response.data.retrievedCollections))
      }
    })
    .catch((error) => {
        if(error.response.status === 401) {
            alert("Error during collection retrieval!")
        }
    });
  }
  
  const bolognaCoords = [44.494887, 11.3426163]
  const collectionsImages = [
    'https://tourismmedia.italia.it/is/image/mitur/20210305163928-shutterstock-172796825?wid=1080&hei=660&fit=constrain,1&fmt=webp',
    'https://www.offerte-vacanza.com/informazioni/wp-content/uploads/2021/09/lago-di-braies-800x445.jpg', 
    'https://www.paesidelgusto.it/media/2021/12/madonna-di-campiglio.jpg&sharpen&save-as=webp&crop-to-fit&w=1200&h=800&q=76'
  ];
  
  return localStorage.getItem("collections") && localStorage.getItem("imgs") && (
    <div className='main-content'>
      
      <div className='collections'>
        <h1 className='title'>My collections</h1>
        <input type='text' ref={searchRef} className='search-collection' onKeyDown={e => {
          if (e.key === 'Enter') {
            console.log("Searching with: ", searchRef.current.value);
            axios.post('/search', {
              "logged_user": localStorage.getItem("user"),
              "search_text": searchRef.current.value
            }).then(response => {
              if(response.data.status === 200) {
                localStorage.setItem("collections", JSON.stringify(response.data.collections)) // Update new collections
                window.location.reload()
              }
            })
          }
          
          setCollectionList(prev => [e.target.value])
        }}/>
        {
          Object.entries(JSON.parse(localStorage.getItem("collections"))).map( (collection) => {
            return (<CollectionCard className='collection' key={collection[0]} 
            title={collection[1].name} place={'Prova'} prevs={collectionsImages} />)
          })
        }
      </div> 
        <DrawMap />
    </div>
  );
}
export default Dashboard;