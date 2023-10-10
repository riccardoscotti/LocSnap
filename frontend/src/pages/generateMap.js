import '../css/generateMap.css'
import 'leaflet/dist/leaflet.css';
import Button from 'react-bootstrap/Button';
import { MapContainer, Marker, Popup, useMapEvents, TileLayer, GeoJSON, useMap } from "react-leaflet";
import * as L from 'leaflet';
import * as d3 from "d3";
import { useState, useRef, createRef } from 'react';
import "leaflet.heat";
import axios from "axios"
import cIcon from "../cluster_icon.png"
import Slider from '@mui/material/Slider';
import DialogActions from '@mui/material/DialogActions'; 
import DialogContent from '@mui/material/DialogContent'; 
import DialogTitle from '@mui/material/DialogTitle'; 
import DialogContentText from '@mui/material/DialogContentText'; 
import Dialog from '@mui/material/Dialog';
import FormControlLabel from '@mui/material/FormControlLabel';
import Checkbox from '@mui/material/Checkbox';

const base_url = "http://localhost:8080"


const markerIcon = L.icon({
  iconSize: [25, 41],
  iconAnchor: [10, 41],
  popupAnchor: [2, -40],
  iconUrl: "https://unpkg.com/leaflet@1.5.1/dist/images/marker-icon.png",
  shadowUrl: "https://unpkg.com/leaflet@1.5.1/dist/images/marker-shadow.png"
});

const clusterIcon = L.icon({
  iconUrl: cIcon,
  iconRetinaUrl: cIcon,
  iconSize: [40, 40],
  iconAnchor: [10, 40],
  popupAnchor: [2, -40],
});

const GenerateMap = () => {
  localStorage.removeItem("buttonClicked") // Prevent old session saves

  const mapRef = createRef()
  const [open, setOpen] = useState(false); 
  const [checked, setChecked] = useState(true);

  var geoJsonLayer;
  var heatmap;
  var markerGroup;
  var CPMap = new Map(); // Hashmap for photos taken on each country

  const handleClickOpen = () => { 
    setOpen(true); 
  }; 
  
  const handleClose = () => { 
    setOpen(false); 
  };

  function invertChecked(event) {
    setChecked(event.target.checked)
  }
  
  function ColorMap() {
    if (localStorage.getItem("buttonClicked") == "cm") {
      const json = JSON.parse(localStorage.getItem("geojson"))

      Object.entries(JSON.parse(localStorage.getItem("collections"))).map( (collection) => {
        for (let index = 0; index < json.features.length; index++) {
          if (d3.geoContains(json.features[index], [collection[1][1], collection[1][0]])) {
            var countryName = json.features[index].properties.feature_name;
            if (CPMap.has(countryName))
              CPMap.set(countryName, CPMap.get(countryName) + 1);
            else 
              CPMap.set(countryName, 1);
          }
        }
      })

      // Remove heatmap
      mapRef.current.eachLayer(function(layer) {
        if(typeof layer._heat !== "undefined") {
            layer.removeFrom(mapRef.current)
        }
      });

      // Remove old geojson
      mapRef.current.eachLayer(function(layer) {
        if(typeof layer.feature !== "undefined") {
            layer.removeFrom(mapRef.current)
        }
      });

      markerGroup?.removeFrom(mapRef.current) // Remove markers, if present.

      // Update with new geojson
      var newgeoJsonLayer = L.geoJSON(Object(JSON.parse(localStorage.getItem("geojson"))), {
        onEachFeature: function (feature, layer) {
          if (typeof CPMap.get(feature.properties.feature_name) == "undefined") {
            layer.setStyle({
              fillColor: '#00FF00',
              fillOpacity: '0.1'
            })
          } else if (CPMap.get(feature.properties.feature_name) > 0) {
            
            layer.setStyle({
              fillColor: '#00FF00',
              fillOpacity: '0.3'
            })
          } else if (CPMap.get(feature.properties.feature_name) >= 5) {
            layer.setStyle({
              fillColor: '#00FF00',
              fillOpacity: '0.6'
            })
          } else if (CPMap.get(feature.properties.feature_name) >= 10) {
            layer.setStyle({
              fillColor: '#00FF00',
              fillOpacity: '0.8'
            })
          }
        }
      })
      
      newgeoJsonLayer.addTo(mapRef.current);
    }
  }

  // User clicked on an area, showing out relative markers
  function showFeatureMarkers(area, map) {
    markerGroup = L.layerGroup().addTo(map);
    Object.entries(JSON.parse(localStorage.getItem("collections"))).map( (collection) => {
      
      if (d3.geoContains(area, [collection[1][1], collection[1][0]])) {
        var marker = new L.marker([collection[1][0], collection[1][1]], {icon: markerIcon}).addTo(markerGroup);
        marker.bindPopup(collection[0])
      }
    })
  }

  function featureContainer(json, point, map) {
      for (let index = 0; index < json.features.length; index++) {
        if (d3.geoContains(json.features[index], point)) {
          showFeatureMarkers(json.features[index], map)
        }
      }
  }

  function AddGeoJSON() {
    const map = useMap();
    geoJsonLayer = L.geoJSON(Object(JSON.parse(localStorage.getItem("geojson"))))
    
    geoJsonLayer.addTo(map);
  }

  function PhotoPerArea() {
    const map = useMap();
    useMapEvents({
      click(e) {
        if (localStorage.getItem("buttonClicked") == "ppa") {

          map.eachLayer(function(layer) {
            if(typeof layer._heat !== "undefined") {
                layer.removeFrom(map)
            }
          });

          geoJsonLayer = L.geoJSON()
            .addData(Object(JSON.parse(localStorage.getItem("geojson"))))
            .addTo(map);

          featureContainer(
            Object(JSON.parse(localStorage.getItem("geojson"))),
            [e.latlng.lng, e.latlng.lat],
            map
          );
          localStorage.removeItem("buttonClicked")
        }
      }
    })
  }

  function confirmCluster() {
    setOpen(false)
    Cluster()
  }

  function Cluster() {
    
    mapRef.current.eachLayer(function(layer) {
      if( typeof layer.feature !== "undefined" ||
          typeof layer._layers !== "undefined" ||
          typeof layer._center !== "undefined") {
          layer.removeFrom(mapRef.current)
      }
    });

    console.log(mapRef.current._layers);
    // markerGroup?.removeFrom(mapRef.current) // Remove all markers, if present.

    // mapRef.current.addLayer(L.tileLayer("https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png"))


    axios.post(`${base_url}/clusterize`, {
      logged_user: localStorage.getItem("user"),
      num_cluster: 3
    })
    .then((response) => {
      if(response.status === 200) {
        const clusters = response.data.clusters;
        
        var markerGroup = L.layerGroup().addTo(mapRef.current);

        Object.entries(clusters).map( (cluster) => {
          var marker = new L.marker([cluster[1].coords[0], cluster[1].coords[1]], {icon: clusterIcon}).addTo(markerGroup);
          marker.bindPopup(`Photos taken here: ${cluster[1].image_names.length}`)
        })

        localStorage.setItem("clusters", clusters)

      }
    })
    .catch((error) => {
        console.log(error);
    });

    
    localStorage.removeItem("buttonClicked")
  }

  function Heatmap() {
    if (localStorage.getItem("buttonClicked") == "hm") { // Remove GeoJSON
      
      mapRef.current.eachLayer(function(layer) {
        if(typeof layer.feature != "undefined") {
            layer.removeFrom(mapRef.current)
        }
      });

      markerGroup?.removeFrom(mapRef.current) // Remove all markers, if present.

      heatmap = L.heatLayer([], {
        radius: 25,
        minOpacity: .5,
        blur: 15,
        gradient: {
          0.0: 'green',
          0.3: 'yellow',
          1.0: 'red'
        }
      }).addTo(mapRef.current);

      Object.entries(JSON.parse(localStorage.getItem("collections"))).map( (collection) => {
        if (d3.geoContains(Object(JSON.parse(localStorage.getItem("geojson"))),
            [collection[1][1], collection[1][0]])) {
          heatmap.addLatLng([collection[1][0], collection[1][1], 100])
        }
      });

      localStorage.removeItem("buttonClicked")
    }
  }

  const bolognaCoords = [44.494887, 11.3426163]

  return (
      <div id="LeafletMap2">
        
          <div id="menuOptions">
          
              <Button className="menuItem">
                Choose map type
              </Button>
              <Button className="menuItem" onClick={ () => {
                localStorage.setItem("buttonClicked", "hm");
                Heatmap()
              }}>
                Heatmap
              </Button>
              <Button className="menuItem" onClick={
                () => {
                  localStorage.setItem("buttonClicked", "ppa")
                  alert("Select the country you're concerned in");
                  }}>
                  Photo per area
              </Button>
              <Button className="menuItem" onClick={
                () => {
                  localStorage.setItem("buttonClicked", "cm")
                  ColorMap()
                }}>
                Color Map
              </Button>
              <Button className="menuItem" onClick={
                () => {
                  handleClickOpen()
                  // Cluster()
                }}>
                Cluster
              </Button>
          </div>
          
          <MapContainer id="mapContainer2" ref={mapRef} center={bolognaCoords} zoom={13} scrollWheelZoom={true} zoomControl={false} attributionControl={false}>
              <AddGeoJSON />
              <PhotoPerArea />
              <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
          </MapContainer>

          <Dialog open={open} onClose={handleClose}> 
            <DialogTitle> 
              Choose clustering method
            </DialogTitle>
            <DialogContent> 
              <FormControlLabel control={<Checkbox defaultChecked checked={checked} onChange={invertChecked} />}  label="Elbow" />
              <Slider defaultValue={10} valueLabelDisplay="auto" min={2} max={20} disabled={checked} />
            </DialogContent> 
            <DialogActions> 
              <Button onClick={confirmCluster} color="primary"> 
              Confirm 
              </Button>
            </DialogActions> 
          </Dialog> 
      </div>
  )
}

export default GenerateMap;