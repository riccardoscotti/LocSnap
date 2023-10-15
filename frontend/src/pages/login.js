import '../css/login.css'
import { useState } from 'react';
import { Navigate, useNavigate } from 'react-router-dom'
import locsnapLogo from '../locsnap_icon.png'
import axios from "axios";
import sha256 from 'js-sha256';

axios.defaults.baseURL = 'http://localhost:8080'


const handleLogin = (usr, psw, navigate) => {
    axios.post('/login', {
        username: usr,
        password: sha256(psw)
    })
    .then((response) => {
        console.log(response);
        if(response.data.status === 200) {
            localStorage.setItem("user", usr)
            navigate('/dashboard')
        } else {
            alert("Login failed.")
        }
    })
    .catch((error) => {
        if(error.response.status === 401) {
            alert("Error during login phase!")
        }
    });
}

const Logo = () => {
    return (
        <div class='logo'>
            <img id='logo-image' src={locsnapLogo} />
            <h1 id="logo-typography">
                LocSnap
            </h1>
        </div>
    )
}

const LoginForm = () => {
    const [username, setUsername] = useState('')
    const [password, setPassword] = useState('')
    const navigate = useNavigate();

    const handleSubmit = (e) => {
        e.preventDefault();
        handleLogin(username, password, navigate)
    }
    
    return (
        <form className='login-form' onSubmit={handleSubmit}>
            <label for='username'>Username</label>
            <input  type='text' 
                    id='username' 
                    name='username' 
                    value={username}
                    onChange={e => setUsername(e.target.value)}
            />
            <label for='password'>Password</label>
            <input  type='password' 
                    id='password' 
                    name='password'
                    value={password}
                    onChange={e => setPassword(e.target.value)} />
            
            <input type='submit' value='Sign in'></input>
        </form>
    )
}

const Login = () => {
    if(localStorage.getItem("user")) {
        return <Navigate to='/dashboard' />
    }

    return (
        <div className="login">
            <Logo />
            <LoginForm />
            <p id='footer'>New to LocSnap? <span>Sign up</span></p>
        </div>
    )
}

export default Login;