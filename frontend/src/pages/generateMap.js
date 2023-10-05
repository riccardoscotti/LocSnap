import '../css/generateMap.css'
import 'leaflet/dist/leaflet.css';
import { useState, useRef } from 'react';
import Button from 'react-bootstrap/Button';
import { MapContainer, Marker, Popup, useMapEvents, MapConsumer, TileLayer, GeoJSON, useMap } from "react-leaflet";
import * as L from 'leaflet';
import us from '../us.json'
import markerIcon from '../marker-icon.png'
import * as d3 from "d3";

const icon = L.icon({
  iconUrl: 'https://leafletjs.com/examples/custom-icons/leaf-green.png',
  iconSize: [38, 95],
});

const GenerateMap = () => {
  localStorage.removeItem("buttonClicked") // Prevent old session saves

    // User clicked on an area, showing out relative markers
    function showFeatureMarkers(area, map) {
      Object.entries(JSON.parse(localStorage.getItem("collections"))).map( (collection) => {
        if (d3.geoContains(area, [collection[1][1], collection[1][0]])) {
          var marker = new L.marker([collection[1][0], collection[1][1]], {icon: icon}).addTo(map);
        }
      })
    }

    function featureContainer(json, point, map) {
        for (let index = 0; index < json.features.length; index++) {
          if (d3.geoContains(json.features[index], point)) {
            // console.log(point);
            // console.log(json.features[index].properties.feature_name)
            showFeatureMarkers(json.features[index], map)
          }
        }
    }

    function AddGeoJSON() {
      const map = useMap();
      var layerPostalcodes = L.geoJSON()
        .addData(Object(JSON.parse(localStorage.getItem("geojson"))))
        .addTo(map);
    }

    function PhotoPerArea() {
      const map = useMap();
      useMapEvents({
        click(e) {
          if (localStorage.getItem("buttonClicked") == "ppa") {
            featureContainer(
              Object(JSON.parse(localStorage.getItem("geojson"))),
              [e.latlng.lng, e.latlng.lat],
              map
            )
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
                <Button className="menuItem">Cluster</Button>
                <Button className="menuItem" onClick={
                  () => {
                    localStorage.setItem("buttonClicked", "ppa")
                    alert("Select the country you're concerned in");
                    }}>
                    Photo per area
                </Button>
                <Button className="menuItem">Heatmap</Button>
            </div>
            
            <MapContainer id="mapContainer2" center={bolognaCoords} zoom={5} scrollWheelZoom={true} zoomControl={false} attributionControl={false}>
                <AddGeoJSON />
                <PhotoPerArea />
                <TileLayer url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
            </MapContainer>
            
        </div>
    )
}

export default GenerateMap;