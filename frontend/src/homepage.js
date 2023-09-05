import './homepage.css';
import '@fontsource/roboto/400.css';
import Button from '@mui/material/Button';
import axios from "axios";
import sha256 from 'js-sha256';

const base_url = "http://localhost:8080"

function checkServer() {
    axios.post(`${base_url}/check`)
    .then((response) => {
        if(response.status === 200) {
            alert("Server working!")
        } else {
            alert("Server not working!")
        }
    });
}

function login() {

    axios.post(`${base_url}/login`, {username: "ChristianP01", password: sha256("christian123")})
    .then((response) => {
        if(response.status === 200) {
            alert("Successful login!")
        }
    })
    .catch((error) => {
        if(error.response.status === 401) {
            alert("Error during login phase!")
        }
    });
}

function Homepage() {
  return (
    <div style={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center"}}>
        <Button variant="contained" color="primary" onClick={checkServer}>
            Check Server
        </Button>

        <Button variant="contained" color="primary" onClick={login}>
            Login
        </Button>
    </div>
  );
}

export default Homepage;