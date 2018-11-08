import React, { Component } from 'react';
import './App.css';
import { List, fromJS } from 'immutable';

import Map from './Map';

class App extends Component {
  constructor(props) {
    super(props);
    this.state = {
      deviceUpdates: List(),
      uniqueId: new Date().getTime(),
    };
    this.fetchDeviceUpdates = this.fetchDeviceUpdates.bind(this);
    this.fetchDeviceUpdates();
  }

  fetchDeviceUpdates() {
    fetch('http://localhost:8888/getLatestUpdates')
    .then(function(response) {
      return response.json();
    }).then(responseJS => {
      this.setState({
        deviceUpdates: fromJS(responseJS[0]),
      });
    })
    setTimeout(this.fetchDeviceUpdates, 5000);
  }

  locationSendClick(uniqueId) {
    navigator.geolocation.getCurrentPosition(function(location) {
      fetch('http://localhost:8080/addGPSUpdate', {
        method: 'POST',
        headers: {
          'Accept': 'application/json',
          'Content-Type': 'application/json',
        },
        body: JSON.stringify({
          timestamp: new Date().getTime(),
          latitude: location.coords.latitude,
          uniqueId: `${uniqueId}`,
          longitude: location.coords.longitude
        })
      });
    });
  }

  render() {
    const { uniqueId } = this.state;
    return (
      <div className="App">
        <Map deviceLocations={this.state.deviceUpdates} />
        <div className="SendButtonWrapper">
          <div>
            Je unique id: <input type="text" value={uniqueId} onChange={(e) => this.setState({ uniqueId: e.target.value })} />
          </div>
          <br />
          <button onClick={() => this.locationSendClick(uniqueId)} className="SendButton">
            Klik om je locatie te sturen!
          </button>
        </div>
      </div>
    );
  }
}

export default App;
