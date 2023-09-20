import '../css/generate.css'
import  Dropdown from 'react-bootstrap/Dropdown'
import DropdownButton from 'react-bootstrap/DropdownButton'

const Generate = () => {
    return (
        <div id='generate'>
            <DropdownButton id='dropdown-basic-button' title='Choose map type'>
                <Dropdown.Item>Cluster</Dropdown.Item>
            </DropdownButton>
        </div>
    )
}

export default Generate;