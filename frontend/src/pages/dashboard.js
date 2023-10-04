import '../css/dashboard.css';
import CollectionCard from '../components/collectionCard';
import { MapContainer, Marker, Popup, useMapEvents, MapConsumer, TileLayer, GeoJSON, useMap } from "react-leaflet";
import 'leaflet/dist/leaflet.css';
import React, { useState, useEffect, useLayoutEffect } from 'react';
import axios from "axios";
import { useLocalStorage } from '../hooks/useLocalStorage';
import varchi from '../varchi.json'
import regioni from '../regioniit.json'
import europe from '../europe.json'
import * as L from 'leaflet';
import markerIcon from '../marker-icon.png'
import * as d3 from "d3";

const base_url = "http://localhost:8080"

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

const feature = {
  "type": "Feature",
  "geometry": {
    "type": "Point",
    "coordinates": [11.343301, 44.492151]
  },
  "properties": {
    "Nome": "RITA - Via Archiginnasio"
  }
};
const position = [feature.geometry.coordinates[1], feature.geometry.coordinates[0]];
const icon = L.icon({
  iconUrl: 'https://leafletjs.com/examples/custom-icons/leaf-green.png',
  icon: mIcon,
  iconSize: [38, 95],
});
  
  // var bo;
  // for (let index = 0; index < regioni.features.length; index++) {
  //   if (regioni.features[index].properties.prov_name == "Bologna")
  //     bo = regioni.features[index]
  // }

  // console.log(d3.geoContains(bo, [11.327591, 44.498955]));

  const [user, setUser] = useLocalStorage("user", null)
  const [collections, setCollections] = useState(null)

  useEffect(() => {
    retrieveCollections(setCollections)
  }, []);

  const retrieveCollections = () => {
    axios.post(`${base_url}/retrievecollections`, {logged_user: user})
    .then((response) => {
      if(response.status === 200) {
        setCollections(response.data.retrievedCollections)
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

  function showMarkersOfFeature(feature) {
    console.log(feature.properties.feature_name);
  }

  function featureContainer(json, point) {
      for (let index = 0; index < json.features.length; index++) {
        if (d3.geoContains(json.features[index], point)) {
          showMarkersOfFeature(json.features[index])
        }
      }
  }

  function GetCoords() {
    useMapEvents({
      click(e) {
        featureContainer(europe, [e.latlng.lng, e.latlng.lat])
      }
    })
  }

  function UploadGeoJSON() {
    const map = useMap()
    useMapEvents({
      click(e) {
        return L.marker(e.latlng).addTo(map);
      }
    })
  }

  return localStorage.getItem("collections") && (
    <div className='main-content'>
      <div className='collections'>
        <h1 className='title'>My collections</h1>
        <input type='text' className='search-collection'/>
        {
          Object.entries(JSON.parse(localStorage.getItem("collections"))).map( (name) => {
            return (<CollectionCard className='collection' key={name[0]} title={name[0]} place={`Coords: ${name[1]}`} prevs={collectionsImages} />)
          })
        }
      </div> 
      <MapContainer id='map-container' center={bolognaCoords} zoom={14} scrollWheelZoom={true} zoomControl={false} attributionControl={false}>
      <GetCoords />
      <UploadGeoJSON />
      <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
      </MapContainer>
    </div>
  );
}
export default Dashboard;