import React, { Component } from 'react';
import ReactMapboxGl, { Layer, Feature } from "react-mapbox-gl";

const GLMap = ReactMapboxGl({});

export default class Map extends Component {
  constructor(props) {
    super(props);
    this.state = {
      zoom: [13],
      center: [5.646842, 52.016295],
    }
  }

  render() {
    const { deviceLocations } = this.props;
    const { zoom, center } = this.state;
    return (
      <GLMap
        style='https://maps.tilehosting.com/styles/basic/style.json?key=fxJrA1DCB2nPe6m5cWXa' //eslint-disable-line
        containerStyle={{
          height: '70vh',
          width: '100vw'
        }}
        center={center}
        zoom={zoom}
      >
        <Layer
          type='circle'
          id='marker'
          paint={{
            'circle-radius': 5,
            'circle-color': '#007cbf'
          }}
        >
        {deviceLocations.map(p => {
          return <Feature coordinates={[p.get('longitude'), p.get('latitude')]} key={p.get('uniqueId')}/>
        }).toArray()}

        </Layer>
      </GLMap>
    )
  }
}
