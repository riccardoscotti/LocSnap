import '../css/generateMap.css'
import 'leaflet/dist/leaflet.css';
import Button from 'react-bootstrap/Button';
import { MapContainer, Marker, Popup, useMapEvents, TileLayer, GeoJSON, useMap } from "react-leaflet";
import * as L from 'leaflet';
import * as d3 from "d3";
import { useState, useRef } from 'react';
import "leaflet.heat";

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

    // User clicked on an area, showing out relative markers
    function showFeatureMarkers(area, map) {
      markerGroup = L.layerGroup().addTo(map);
      Object.entries(JSON.parse(localStorage.getItem("collections"))).map( (collection) => {
        if (d3.geoContains(area, [collection[1][1], collection[1][0]])) {
          var marker = new L.marker([collection[1][0], collection[1][1]], {icon: markerIcon}).addTo(markerGroup);
          marker.bindPopup(collection[0])
        }
      })
      // console.log(markerGroup);
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
      geoJsonLayer = L.geoJSON()
        .addData(Object(JSON.parse(localStorage.getItem("geojson"))))

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
                0.5: 'yellow',
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
                <Button className="menuItem">
                  Cluster
                </Button>
            </div>
            
            <MapContainer id="mapContainer2" ref={mapRef} center={bolognaCoords} zoom={13} scrollWheelZoom={true} zoomControl={false} attributionControl={false}>
                <AddGeoJSON />
                <PhotoPerArea />
                <Heatmap />
                <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
            </MapContainer>
            
        </div>
    )
}

export default GenerateMap;