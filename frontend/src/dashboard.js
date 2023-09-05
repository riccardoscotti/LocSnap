import './dashboard.css';
import '@fontsource/roboto/400.css';
import Login from './login'
import locsnapLogo from './locsnap_icon.png'

function Dashboard() {
  return (
    <div style={{
        display: "flex",
        justifyContent: "center",
        alignItems: "center"}}>
        <Login />
    </div>
  );
}

export default Dashboard;