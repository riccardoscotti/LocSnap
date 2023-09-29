import '../css/dashboard.css';
import CollectionCard from '../components/collectionCard';
import { MapContainer, Marker, Popup, useMapEvents, TileLayer } from "react-leaflet";
import 'leaflet/dist/leaflet.css';
import React, { useState, useEffect } from 'react';
import markerIcon from '../marker-icon.png'
import * as L from "leaflet";

function Dashboard() {
  
  const bolognaCoords = [44.494887, 11.3426163]
  const collections = [
    'https://tourismmedia.italia.it/is/image/mitur/20210305163928-shutterstock-172796825?wid=1080&hei=660&fit=constrain,1&fmt=webp',
    'https://www.offerte-vacanza.com/informazioni/wp-content/uploads/2021/09/lago-di-braies-800x445.jpg', 
    'https://www.paesidelgusto.it/media/2021/12/madonna-di-campiglio.jpg&sharpen&save-as=webp&crop-to-fit&w=1200&h=800&q=76'
  ];

  const mIcon = new L.Icon({
    iconUrl: markerIcon,
    iconRetinaUrl: markerIcon,
    iconSize: [45, 48],
    shadowSize: [50, 64],
    iconAnchor: [22, 94],
    shadowAnchor: [4, 62],
    popupAnchor: [-3, -76],
  })

  return (
    <div className='main-content'>
      <div className='collections'>
        <h1 className='title'>My collections</h1>
        <input type='text' className='search-collection'/>
        <CollectionCard title={'Gita in montagna'} place={'Madonna di Campiglio'} prevs={collections} />
      </div> 
      <MapContainer id='map-container' center={bolognaCoords} zoom={14} scrollWheelZoom={true} zoomControl={false} attributionControl={false}>
        <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
        {/* <Markers/> */}
        <Marker position={[44.494887, 11.3426163]} icon={mIcon}>
          
        </Marker>
      </MapContainer>
    </div>
  );
}
export default Dashboard;