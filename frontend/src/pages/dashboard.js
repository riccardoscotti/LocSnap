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
  const [toast, setToast] = useState(false)
  let [loaded, setLoaded] = useState(false)
  const mapRef = createRef()
  let publicPhotoMarkers = L.layerGroup();
  let [openPhotosStatus, setOpenPhotosStatus] = useState(false)

  function loadData() {
    return axios.post('/retrieveimages', {
      logged_user: localStorage.getItem("user")
    })
    .then((response) => {
      if(response.data.status === 200) {
        localStorage.setItem("imgs", JSON.stringify(response.data.imgs))
        axios.post('/get_friends', {
          logged_user: localStorage.getItem('user')
        })
        .then((response) => {
            if(response.data.status === 200) {
              localStorage.setItem("friends", JSON.stringify(response.data.friends))
              axios.post("/retrievecollections", {
                logged_user: localStorage.getItem("user")
              },
              header_config  
              )
              .then((response) => {
                if(response.status === 200) {
                  localStorage.setItem("collections", JSON.stringify(response.data.retrieved_collections))
                  setLoaded(true)
                }
              })
              .catch((error) => {
                if(error.response.status === 401) {
                  alert("Error during collection retrieval!")
                }
              });
            }
        })
        .catch((error) => {
            if(error.response.status === 401) {
                alert("Error during friend addition.")
            }
        })
      }
      return response
    })
    .catch((error) => {
        console.log(error);
    });
  }

  useEffect(() => {
    loadData()
  }, [])

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
      <div className="form-check">
        <input  className="form-check-input" 
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

  function loadPublicPhotos() {
    axios.post('/retrieve_public', {
      logged_user: localStorage.getItem("user")
    })
    .then((response) => {
      if(response.data.status === 200) {
        publicPhotoMarkers.addTo(mapRef.current);

        Object.entries(response.data.public_photos).map( (img) => {
          let marker = new L.marker([img[1].coords[0], img[1].coords[1]], {icon: publicMarker}).addTo(publicPhotoMarkers);
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
      let userPhotosMarkers = L.layerGroup().addTo(mapRef.current);
      Object.entries(JSON.parse(localStorage.getItem("imgs"))).map( (img) => {
        let marker = new L.marker([img[1].coords[0], img[1].coords[1]], {icon: mIcon}).addTo(userPhotosMarkers);
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

  function renderPage() {
    
    if (loaded) {
      return (
        <div className='main-content'>
          <div className='collections'>
            <h1 id='title'>My collections</h1>
            <input type='text' ref={searchRef} className='search-collection' onKeyDown={e => {
              if (e.key === 'Enter') {
                setCollectionList(prev => [e.target.value])
                
                axios.post('/search', {
                  "logged_user": localStorage.getItem("user"),
                  "search_text": searchRef.current.value.trim()
                }).then(response => {
                  if(response.data.status === 200) {
                    localStorage.setItem("collections", JSON.stringify(response.data.collections)) // Update new collections
                    window.location.reload()
                  }
                })
              }
            }}/>
            {
              Object.entries(JSON.parse(localStorage.getItem("collections"))).map( (collection) => {
                return (
                  <div key={collection} onClick={(e) => {
                    if (e.target.firstChild !== null && e.target.firstChild.data !== "undefined") {
                      localStorage.setItem("clickedColl", e.target.firstChild.data)
                      openPhotosFunc()
                    }
                    } }>
                    <CollectionCard 
                      className='collection' 
                      key={collection}
                      title={collection[1].name} 
                      place={collection[1].place}
                    />
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
      )
    } else{
      return (
        <div>
          <p id="loading">
            Loading...
          </p>
        </div>
      )
      
    }
    
  }
  
  // return localStorage.getItem("collections") && localStorage.getItem("imgs") && (
    return (
      renderPage()
  );
}
export default Dashboard;