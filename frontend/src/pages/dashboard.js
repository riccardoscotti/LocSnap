import '../css/dashboard.css';
import CollectionCard from '../components/collectionCard';
import { MapContainer, TileLayer } from "react-leaflet";
import 'leaflet/dist/leaflet.css';
import React, { useEffect, useState, createRef, useRef, useCallback } from 'react';
import axios from "axios";
import Modal from 'react-bootstrap/Modal';

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

  const publicMarker = new L.Icon({
    iconUrl: 'https://raw.githubusercontent.com/pointhi/leaflet-color-markers/master/img/marker-icon-2x-green.png',
    iconRetinaUrl: markerIcon,
    iconSize: [40, 50],
    shadowSize: [50, 64],
    iconAnchor: [22, 94],
    shadowAnchor: [4, 62],
    popupAnchor: [-3, -76]
  })

  let [searchText, setSearchText] = useState("")
  let [collectionList, setCollectionList] = useState(["collezione1"])
  const searchRef = createRef()
  const [showPublic, setShowPublic] = useState(false)
  const mapRef = createRef()
  var publicPhotoMarkers = L.layerGroup();
  let [openPhotosStatus, setOpenPhotosStatus] = useState(false)

  // Collections initialization
  useEffect(() => {
    retrieveCollections();
    loadImages();
    loadFriends();
  }, []);

  function MapHookCB() {
    const mapCB = useCallback(node => {
      if (node) {
        mapRef.current = node
        showMarkers()
      }
    }, [])

    return [mapCB];
  }

  function DrawMap() {
    const [innerMapRef] = MapHookCB()
    
    return (
      <MapContainer id='map-container' ref={innerMapRef} center={bolognaCoords} zoom={14} scrollWheelZoom={true} zoomControl={false} attributionControl={false}>
        <TileLayer id='tile-layer' url="https://{s}.tile.openstreetmap.org/{z}/{x}/{y}.png" />
      </MapContainer>
    )
  }

  function CheckBoxPublic() {
    return (
      <div class="form-check">
        <input  class="form-check-input" 
                type="checkbox" 
                id="publicCheckBox" 
                // checked={showPublic}
                onChange={(e) => {
                  if (e.target.checked) {
                    // setShowPublic(true)
                    loadPublicPhotos()
                  } else {
                    // setShowPublic(false)
                    publicPhotoMarkers?.removeFrom(mapRef.current)
                  }

                }
        } />
        <p id="labelPCB"> Show public photos </p>
      </div>
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

  function loadPublicPhotos() {
    axios.post('/retrieve_public', {
      logged_user: localStorage.getItem("user")
    })
    .then((response) => {
      if(response.data.status === 200) {
        publicPhotoMarkers.addTo(mapRef.current);

        Object.entries(response.data.public_photos).map( (img) => {
          var marker = new L.marker([img[1].coords[0], img[1].coords[1]], {icon: publicMarker}).addTo(publicPhotoMarkers);
          marker.bindPopup(`${img[1].author}: ${img[1].name}`);
        })
      }
    })
    .catch((error) => {
        console.log(error);
    });
  }

function loadFriends() {
    axios.post('/get_friends', {
        logged_user: localStorage.getItem('user')
    })
    .then((response) => {
        if(response.data.status === 200) {
            localStorage.setItem("friends", JSON.stringify(response.data.friends))
        }
    })
    .catch((error) => {
        if(error.response.status === 401) {
            alert("Error during friend addition.")
        }
    })
}

  function showMarkers() {
    if (mapRef.current !== "undefined") {
      var userPhotosMarkers = L.layerGroup().addTo(mapRef.current);
      Object.entries(JSON.parse(localStorage.getItem("imgs"))).map( (img) => {
        var marker = new L.marker([img[1].coords[0], img[1].coords[1]], {icon: mIcon}).addTo(userPhotosMarkers);
        marker.bindPopup(img[1].name);
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
        localStorage.setItem("collections", JSON.stringify(response.data.retrieved_collections))
      }
    })
    .catch((error) => {
      if(error.response.status === 401) {
        alert("Error during collection retrieval!")
      }
    });
  }

  function OpenPhotos(props) {

    return localStorage.getItem("previews") && (
      <Modal
        {...props}
          size="md"
          aria-labelledby="contained-modal-title-vcenter"
          centered >
          <Modal.Header>
            <Modal.Title id="contained-modal-title-vcenter">
              Photos of {localStorage.getItem("clickedColl")}
            </Modal.Title>
          </Modal.Header>
          <Modal.Body id='social-body'>
            {
              Object.entries(JSON.parse(localStorage.getItem("previews"))).map( (img) => {
                return (
                  <div>
                    <p>...</p>
                    <img src={"data:image/jpg;base64," + img[1] } width="250vw" height="300vh" />
                  </div>      
                )
              })
            }
          </Modal.Body>
        </Modal>
    )
  }

  function openPhotosFunc() {
    let arrayBase64 = []
    axios.post("/imagesof", {
      logged_user: localStorage.getItem('user'),
      collection_name: localStorage.getItem("clickedColl")
    }).then(res => {
      Object.entries(res.data.images).map(img => {
        arrayBase64.push(img[1].image)
      })

      localStorage.setItem("previews", JSON.stringify(arrayBase64))
      setOpenPhotosStatus(true)
    })
  }

  function hideOpenPhotos() {
    setOpenPhotosStatus(false)
    localStorage.removeItem("clickedColl")
    localStorage.removeItem("previews")
  }
  
  const bolognaCoords = [44.494887, 11.3426163]
  const collectionsImages = [
    'https://tourismmedia.italia.it/is/image/mitur/20210305163928-shutterstock-172796825?wid=1080&hei=660&fit=constrain,1&fmt=webp',
    'https://www.offerte-vacanza.com/informazioni/wp-content/uploads/2021/09/lago-di-braies-800x445.jpg', 
    'https://www.paesidelgusto.it/media/2021/12/madonna-di-campiglio.jpg&sharpen&save-as=webp&crop-to-fit&w=1200&h=800&q=76'
  ];
  
  // return localStorage.getItem("collections") && localStorage.getItem("imgs") && (
    return localStorage.getItem("collections") && localStorage.getItem("imgs") && (
    <div className='main-content'>
      <div className='collections'>
        <h1 id='title'>My collections</h1>
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
            return (
              <div onClick={(e) => {
                if (e.target.firstChild !== null && e.target.firstChild.data !== "undefined") {
                  localStorage.setItem("clickedColl", e.target.firstChild.data)
                  openPhotosFunc()
                }
                } }>
                <CollectionCard className='collection' key={collection}
                title={collection[1].name} place={collection[1].place} prevs={
                  collectionsImages
                } />
              </div>
            )
          })
        }

        <OpenPhotos
          show={ openPhotosStatus }
          onHide={hideOpenPhotos}
        />
        
      </div> 
        <DrawMap />
        <CheckBoxPublic />
        <OpenPhotos />
    </div>


  );
}
export default Dashboard;