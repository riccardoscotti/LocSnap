import '../css/generateMap.css'
import 'leaflet/dist/leaflet.css';
import Button from 'react-bootstrap/Button';
import { MapContainer, Marker, Popup, useMapEvents, TileLayer, GeoJSON, useMap } from "react-leaflet";
import * as L from 'leaflet';
import * as d3 from "d3";
import { useState, useRef, createRef, useEffect } from 'react';
import "leaflet.heat";
import axios from "axios"
import cIcon from "../cluster_icon.png"
import Slider from '@mui/material/Slider';
import FormControlLabel from '@mui/material/FormControlLabel';
import Modal from 'react-bootstrap/Modal';
import Checkbox from '@mui/material/Checkbox';

<link
  rel="stylesheet"
  href="https://cdn.jsdelivr.net/npm/bootstrap@5.3.0/dist/css/bootstrap.min.css"
  integrity="sha384-9ndCyUaIbzAi2FUVXJi0CjmCapSmO7SnpJef0486qhLnuZ2cdeRhO02iuK6FUUVM"
  crossorigin="anonymous"
/>

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
  // Prevent old session saves
  localStorage.removeItem("mapIntent")
  localStorage.removeItem("geojson")
  localStorage.removeItem("clusters")

  const mapRef = createRef()
  const [open, setOpen] = useState(false);
  const [checked, setChecked] = useState(true);
  const [received, setReceived] = useState(false);
  const uploadFilterRef = useRef(null);
  const [map, setMap] = useState(null);

  var geoJsonLayer;
  var coloredGeoJsonlayer;
  var heatmap;
  var markerGroup;
  var clusterGroups;
  var CPMap = new Map(); // Hashmap for photos taken on each country

  function uploadFilter (event) {

    const fileObj = event.target.files[0];
    const reader = new FileReader();

    reader.onload = function() {
        localStorage.setItem("geojson", reader.result)
        AddGeoJSON()
    }
    reader.readAsText(fileObj)
    event.target.value = null;
  }

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
    const json = JSON.parse(localStorage.getItem("geojson"))

    Object.entries(JSON.parse(localStorage.getItem("imgs"))).map( (img) => {
      for (let index = 0; index < json.features.length; index++) {
        if (d3.geoContains(json.features[index], [img[1].coords[1], img[1].coords[0]])) {
          var countryName = json.features[index].properties.feature_name;
          if (CPMap.has(countryName))
            CPMap.set(countryName, CPMap.get(countryName) + 1);
          else 
            CPMap.set(countryName, 1);
        }
      }
    })

    // Remove heatmap
    map.eachLayer(function(layer) {
      if(typeof layer._heat !== "undefined") {
          layer.removeFrom(map)
      }
    });

    // Remove old geojson
    map.eachLayer(function(layer) {
      if(typeof layer.feature !== "undefined") {
          layer.removeFrom(map)
      }
    });

    markerGroup?.removeFrom(map) // Remove markers, if present.

    // Update with new geojson
    coloredGeoJsonlayer = L.geoJSON(Object(JSON.parse(localStorage.getItem("geojson"))), {
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
    
    coloredGeoJsonlayer.addTo(map);
  }

  // User clicked on an area, showing out relative markers
  function showFeatureMarkers(area, map) {
    markerGroup = L.layerGroup().addTo(map);
    Object.entries(JSON.parse(localStorage.getItem("imgs"))).map( (img) => {
      
      if (d3.geoContains(area, [img[1].coords[1], img[1].coords[0]])) {
        var marker = new L.marker([img[1].coords[0], img[1].coords[1]], {icon: markerIcon}).addTo(markerGroup);
        marker.bindPopup(img[1].name)
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
    geoJsonLayer = L.geoJSON(Object(JSON.parse(localStorage.getItem("geojson"))))
    geoJsonLayer.addTo(map);
  }

  function PhotoPerArea() {
      const map = useMap();
      useMapEvents({
        click(e) {
          if (localStorage.getItem("geojson") && localStorage.getItem("mapIntent") === "ppa") {
  
              map.eachLayer(function(layer) {
                if(typeof layer._heat !== "undefined") {
                    layer.removeFrom(map)
                }
              });

              if (!map.hasLayer(geoJsonLayer)) {
                geoJsonLayer = L.geoJSON()
                .addData(Object(JSON.parse(localStorage.getItem("geojson"))))
                .addTo(map);
              }

              coloredGeoJsonlayer?.removeFrom(map)
              
              featureContainer(
                Object(JSON.parse(localStorage.getItem("geojson"))),
                [e.latlng.lng, e.latlng.lat],
                map
              );
              
              localStorage.removeItem("mapIntent")
          }
        }
      })
  }

  // useEffect(() => {
  //   if (confirmed == true) {
  //     Cluster();
  //   }
  // }, [confirmed])

  function ConfirmCluster() {
    setOpen(false);
    Cluster();
  }

  async function Cluster() {

    localStorage.removeItem("clusters") // Updates new value
    
    map.eachLayer(function(layer) {
      if( typeof layer.feature !== "undefined" ||
      typeof layer._layers !== "undefined" ||
      typeof layer._center !== "undefined" ||
      typeof layer._heat !== "undefined") {
        layer.removeFrom(map)
      }
    });
    
    markerGroup?.removeFrom(map) // Remove all markers, if present. 
    clusterGroups?.removeFrom(map) // Remove all clusters, if present.

    await axios.post(`${base_url}/clusterize`, {
      logged_user: localStorage.getItem("user"),
      num_cluster: localStorage.getItem("numCluster"), 
      elbow: checked
    })
    .then((response) => {
      if(response.data.status === 200) {
        // console.log(response.data.clusters);
        const clusters = response.data.clusters;

        localStorage.setItem("clusters", JSON.stringify(clusters))
      }
    })
    .catch((error) => {
      console.log("Error...");
        console.log(error);
    });

    clusterGroups = L.layerGroup()
    clusterGroups.addTo(map);

    Object.entries(JSON.parse(localStorage.getItem("clusters"))).map( (cluster) => {
      console.log(cluster);
      var marker = new L.marker([cluster[1].centroid[0], cluster[1].centroid[1]], {icon: clusterIcon}).addTo(clusterGroups);
      marker.bindPopup(`Photos taken here: ${cluster[1].images.length}`)
    })

    localStorage.removeItem("mapIntent")
  }

  // useEffect(() => {
  //   if (received == true) {
  //     alert("received")
  //   }
  // }, received)

  function Heatmap() {

    // map.eachLayer(function(layer) {
    //   if(typeof layer.feature != "undefined" ||
    //      typeof layer._heat != "undefined") {
    //       layer.removeFrom(map)
    //   }
    // });

    markerGroup?.removeFrom(map) // Remove all markers, if present.
    clusterGroups?.removeFrom(map)
    geoJsonLayer?.removeFrom(map)
    coloredGeoJsonlayer?.removeFrom(map)

    heatmap = L.heatLayer([], {
      radius: 25,
      minOpacity: .5,
      blur: 15,
      gradient: {
        0.0: 'green',
        0.3: 'yellow',
        1.0: 'red'
      }
    })

    heatmap.addTo(map);

    Object.entries(JSON.parse(localStorage.getItem("imgs"))).map( (img) => {
      heatmap.addLatLng([img[1].coords[0], img[1].coords[1], 100])
    });
  }

  const bolognaCoords = [44.494887, 11.3426163]

  function ClusteringModal(props) {
    return (
      <Modal
        {...props}
        size="md"
        aria-labelledby="contained-modal-title-vcenter"
        centered >
        <Modal.Header>
          <Modal.Title id="contained-modal-title-vcenter">
            Select clustering technique
          </Modal.Title>
        </Modal.Header>
        <Modal.Body>
          <FormControlLabel id="elbowLabel" control={<Checkbox checked={checked} onChange={invertChecked} />} label="Elbow" />
          <Slider 
          onChange={(e, val) => localStorage.setItem("numCluster", val)}
          onChangeCommitted={(e, val) => localStorage.setItem("numCluster", val)}
          defaultValue={10}
          valueLabelDisplay="auto"
          min={2}
          max={20}
          disabled={checked} />
        </Modal.Body>
        <Modal.Footer>
          <Button id="confirmButton" onClick={ConfirmCluster}>Confirm</Button>
        </Modal.Footer>
      </Modal>
    );
  }

  return (
      <div id="LeafletMap2">
          <div id="menuOptions">
              <input
                  style={{display: 'none'}}
                  ref={uploadFilterRef}
                  type="file"
                  accept=".geojson, .json" 
                  onChange={uploadFilter}
              />
              <Button className="menuItem" onClick={
                 () => {
                  uploadFilterRef.current.click()
                 }
              }>
                Load GeoJSON
              </Button>
              <Button className="menuItem" onClick={() => {
                localStorage.removeItem("mapIntent")
                Heatmap()
              }}>
                Heatmap
              </Button>
              <Button className="menuItem" onClick={
                () => {
                  localStorage.setItem("mapIntent", "ppa")

                  if (localStorage.getItem("geojson") == null) {
                    alert("You must upload a GeoJSON file first.")
                  } else {
                    
                    alert("Select the country you're concerned in")
                  }
                  }}>
                  Photo per area
              </Button>
              <Button className="menuItem" onClick={
                () => {
                  if (localStorage.getItem("geojson") == null) {
                    alert("You must upload a GeoJSON file first.")
                  } else {
                    localStorage.removeItem("mapIntent")
                    ColorMap()
                  }
                }}>
                Color Map
              </Button>
              <Button className="menuItem" onClick={
                () => {
                  localStorage.removeItem("mapIntent")
                  handleClickOpen()
                }}>
                Cluster
              </Button>
          </div>
          
          <MapContainer id="mapContainer2" ref={setMap} center={bolognaCoords} zoom={13} scrollWheelZoom={true} zoomControl={false} attributionControl={false}>
              <PhotoPerArea />
              <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
          </MapContainer>

          <ClusteringModal
            show={open}
            onHide={() => setOpen(false)}
          />          
      </div>
  )
}

export default GenerateMap;