import '../css/generateMap.css'
import 'leaflet/dist/leaflet.css';
import Button from 'react-bootstrap/Button';
import { MapContainer, Marker, Popup, useMapEvents, TileLayer, GeoJSON, useMap } from "react-leaflet";
import * as L from 'leaflet';
import * as d3 from "d3";
import { useState, useRef } from 'react';
import "leaflet.heat";
import axios from "axios"

const base_url = "http://localhost:8080"


const markerIcon = L.icon({
  iconSize: [25, 41],
  iconAnchor: [10, 41],
  popupAnchor: [2, -40],
  // specify the path here
  iconUrl: "https://unpkg.com/leaflet@1.5.1/dist/images/marker-icon.png",
  shadowUrl: "https://unpkg.com/leaflet@1.5.1/dist/images/marker-shadow.png"
});

const GenerateMap = () => {
  localStorage.removeItem("buttonClicked") // Prevent old session saves
  
  const mapRef = useRef(null)
  var geoJsonLayer;
  var heatmap;
  var markerGroup;

  var CPMap = new Map(); // Hashmap for photos taken on each country

  function checkCountry(area, coords) {
    for (let index = 0; index < area.features.length; index++) {
      if (d3.geoContains(area.features[index], coords)) {
        return area.features[index].feature_name
      }
    }
  }
  
  function ColorMap() {
    const map = useMap()
    useMapEvents({
      click() {

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
          
          newgeoJsonLayer.addTo(map);
        }
      }
    })
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

  function Cluster() {
    const map = useMap()
    useMapEvents({
      click() {
        if (localStorage.getItem("buttonClicked") == "cst") {
          map.eachLayer(function(layer) {
            if(typeof layer.feature !== "undefined") {
                layer.removeFrom(map)
            }
          });

          markerGroup?.removeFrom(map) // Remove all markers, if present.

          axios.post(`${base_url}/clusterize`, {
            logged_user: localStorage.getItem("user"),
            num_cluster: 3
          })
          .then((response) => {
            if(response.status === 200) {
              const clusters = response.data.clusters;
              var markerGroup = L.layerGroup().addTo(map);

              Object.entries(clusters).map( (cluster) => {
                var marker = new L.marker([cluster[1].coords[0], cluster[1].coords[1]], {icon: markerIcon}).addTo(markerGroup);
                marker.bindPopup(`Photos taken here: ${cluster[1].image_names.length}`)
              })

            }
          })
          .catch((error) => {
              console.log(error);
          });

          localStorage.removeItem("buttonClicked")
        }
      }
    })
  }

  function Heatmap() {
    const map = useMap();
    useMapEvents({
      click() {
        if (localStorage.getItem("buttonClicked") == "hm") { // Remove GeoJSON
          
          map.eachLayer(function(layer) {
            if(typeof layer.feature !== "undefined") {
                layer.removeFrom(map)
            }
          });

          markerGroup?.removeFrom(map) // Remove all markers, if present.

          heatmap = L.heatLayer([], {
            radius: 25,
            minOpacity: .5,
            blur: 15,
            gradient: {
              0.0: 'green',
              0.3: 'yellow',
              1.0: 'red'
            }
          }).addTo(map);

          Object.entries(JSON.parse(localStorage.getItem("collections"))).map( (collection) => {
            if (d3.geoContains(Object(JSON.parse(localStorage.getItem("geojson"))),
                [collection[1][1], collection[1][0]])) {
              heatmap.addLatLng([collection[1][0], collection[1][1], 100])
            }
          });

          localStorage.removeItem("buttonClicked")
        }
      }
    })
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
                alert("Select the map you want to apply the heatmap to");
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
                  alert("Click the map to color it")
                }}>
                Color Map
              </Button>
              <Button className="menuItem" onClick={
                () => {
                  localStorage.setItem("buttonClicked", "cst")
                  alert("Click the map to clusterize it")
                }}>
                Cluster
              </Button>
          </div>
          
          <MapContainer id="mapContainer2" ref={mapRef} center={bolognaCoords} zoom={13} scrollWheelZoom={true} zoomControl={false} attributionControl={false}>
              <AddGeoJSON />
              <PhotoPerArea />
              <Heatmap />
              <ColorMap />
              <Cluster />
              <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
          </MapContainer>
          
      </div>
  )
}

export default GenerateMap;