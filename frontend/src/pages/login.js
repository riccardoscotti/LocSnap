import '../css/login.css'
import { useState } from 'react';
import { Navigate } from 'react-router-dom'
import locsnapLogo from '../locsnap_icon.png'
import axios from "axios";
import sha256 from 'js-sha256';

const base_url = "http://localhost:8080"


// function handleLogin(usr, psw) {

//     axios.post(`${base_url}/login`, {username: usr, password: sha256(psw)})
//     .then((response) => {
//         if(response.status === 200) {
//             alert("Successful login!")
//             return <Navigate to='/dashboard' />
//         }
//     })
//     .catch((error) => {
//         if(error.response.status === 401) {
//             alert("Error during login phase!")
//         }
//     });
// }

const handleLogin = (usr, psw) => {
    
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

    const handleSubmit = (e) => {
        e.preventDefault();
        handleLogin(username, password)
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
    return (
        <div className="login">
            <Logo />
            <LoginForm />
            {/* Chiamata a loginForm() */}
            <p id='footer'>New to LocSnap? <span>Sign up</span></p>
        </div>
    )
}

export default Login;