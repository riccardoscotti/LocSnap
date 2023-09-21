import '../css/generateMap.css'
import  Dropdown from 'react-bootstrap/Dropdown'
import DropdownButton from 'react-bootstrap/DropdownButton'
import { useState } from 'react';

const GenerateMap = () => {

    return (
        <div id='generate'>
            <DropdownButton id='dropdown-basic-button' title='Choose map type'>
                <div id="backgroundItems">
                    <Dropdown.Item className="dropdown-item">Cluster</Dropdown.Item>
                    <Dropdown.Item className="dropdown-item">Photo per area</Dropdown.Item>
                    <Dropdown.Item className="dropdown-item">Heatmap</Dropdown.Item>
                </div>
            </DropdownButton>
        </div>
    )
}

export default GenerateMap;