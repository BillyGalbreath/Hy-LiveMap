window.onload = function () {
  let map = new LiveMap();
  map.setView([0, 0], 3);
  console.log(map.getZoom());
}

const zoomMaxIn = 2;
const zoomMaxOut = 3;

class LiveMap extends L.map {

  constructor() {
    super("map", {
      // use a flat and simple crs
      crs: L.Util.extend(L.CRS.Simple, {
        // we need to flip the y-axis correctly
        // https://stackoverflow.com/a/62320569/3530727
        transformation: new L.Transformation(1, 0, 1, 0)
      }),
      // center map on spawn
      center: [0, 0],
      // always allow attribution in case a layer needs it
      attributionControl: true,
      // canvas is more efficient than svg
      preferCanvas: true,
      // prevent tile reloads from flashing
      fadeAnimation: true,
      // don't add zoom control here, we'll do it manually below the scale control
      zoomControl: true,//todo false,

      // chrome based browsers on linux zoom twice as fast, so we have to double the ratio
      // effectively undoes the fix for Leaflet/Leaflet#4538 and Leaflet/Leaflet#7403
      // https://github.com/Leaflet/Leaflet/commit/96977a19358374b0166cff049862fa1f0fed5948
      //
      // remove this logic when this bug gets fixed: https://issues.chromium.org/issues/40887377
      // it seems intentional, so it might not get fixed https://issues.chromium.org/issues/40804672
      wheelPxPerZoomLevel: L.Browser.linux && L.Browser.chrome ? 120 : 60,
      // these get weird when changed. so don't.
      zoomSnap: 1,
      zoomDelta: 1,

      // for extra zoom in, make higher than maxNativeZoom
      // this is the stretched tiles to zoom in further
      // maxZoom = maxNativeZoom + extra
      // maxZoom = zoom.maxOut - (-zoom.maxIn)
      maxZoom: zoomMaxOut - (-zoomMaxIn),
      // the closest zoomed in possible (without stretching)
      // this is always 0. no exceptions!
      minZoom: 0,
      // do not wrap the layer around the antimeridian
      noWrap: true
    });

    //this.setView([0, 0], 0);

    this.attributionControl.setPrefix("<a href='https://curseforge.com/hytale/mods/livemap'>LiveMap &copy; 2020-2026</a>");

    new LiveMapTileLayer(this);
  }
}

class LiveMapTileLayer extends L.TileLayer {
  constructor(map) {
    super("tiles/world/{zoom}/{x}_{z}.png", {
      // tile sizes match regions sizes (512 blocks x 512 blocks)
      tileSize: 512,
      // the closest zoomed in possible (without stretching)
      // this is always 0. no exceptions!
      minZoom: map.options.minZoom,
      // always the same as map's maxZoom option
      maxZoom: map.options.maxZoom,
      // the closest zoomed in possible (without stretching)
      // this is always 0. no exceptions!
      minNativeZoom: 0,
      // the farthest possible zoomed out possible
      maxNativeZoom: zoomMaxOut,
      // we need to counter effect the higher maxZoom here
      // zoomOffset = maxNativeZoom - maxZoom
      // zoomOffset = zoom.maxOut - (zoom.maxOut + (-zoom.maxIn))
      // zoomOffset = (-zoom.maxIn)
      zoomOffset: -zoomMaxIn,
      // zoom stuff (this is a pita, btw)
      // this doesn't work right, so we leave it false and override _getZoomForUrl below
      zoomReverse: false
    });

    // push this layer to the back (leaflet defaults it to 1)
    this.setZIndex(0);

    this.addTo(map);
  }

  // reverse zoom controls here instead of the flag in options
  _getZoomForUrl() {
    return (this.options.maxZoom - this._tileZoom) + this.options.zoomOffset;
  }

  // customize url to our needs
  getTileUrl(coords) {
    const data = {
      world: "world",
      x: coords.x,
      z: coords.y,
      zoom: this._getZoomForUrl()
    };
    return L.Util.template(this._url, L.Util.extend(data, this.options));
  }

  // https://github.com/Leaflet/Leaflet/issues/6659#issuecomment-491545545
  // https://gist.github.com/barryhunter/e42f0c4756e34d5d07db4a170c7ec680
  refresh() {
    for (const key in this._tiles) {
      const tile = this._tiles[key];
      if (!tile.current || !tile.active) {
        continue;
      }
      const oldSrc = tile.el.src;
      const newSrc = this.getTileUrl(tile.coords);
      if (oldSrc === newSrc) {
        continue;
      }
      this._map._fadeAnimated = false;
      const img = new Image();
      img.onload = () => {
        L.Util.requestAnimFrame(() => {
          tile.el.src = newSrc;
        });
        setTimeout(() => {
          this._map._fadeAnimated = true;
        }, 100);
      }
      img.src = newSrc;
    }
  }

  //
  createTile(coords, done) {
    const tile = document.createElement('img');

    L.DomEvent.on(tile, "load", L.Util.bind(this._tileOnLoad, this, done, tile));
    L.DomEvent.on(tile, "error", L.Util.bind(this._tileOnError, this, done, tile));

    if (this.options.crossOrigin || this.options.crossOrigin === '') {
      tile.crossOrigin = this.options.crossOrigin === true ? '' : this.options.crossOrigin;
    }

    // for this new option we follow the documented behavior
    // more closely by only setting the property when string
    if (typeof this.options.referrerPolicy === 'string') {
      tile.referrerPolicy = this.options.referrerPolicy;
    }

    // The alt attribute is set to the empty string,
    // allowing screen readers to ignore the decorative image tiles.
    // https://www.w3.org/WAI/tutorials/images/decorative/
    // https://www.w3.org/TR/html-aria/#el-img-empty-alt
    tile.alt = "";

    // Set role="presentation" to force screen readers to ignore this
    // https://www.w3.org/TR/wai-aria/roles#textalternativecomputation
    tile.setAttribute('role', 'presentation');

    // Retrieve image via a fetch instead of just setting the src.
    // This works around the fact that browsers usually don't make
    // a request for an image that was previously loaded without
    // resorting to changing the URL (which would break caching).
    fetch(this.getTileUrl(coords))
      .then(res => {
        // Call leaflet's error handler if request fails for some reason
        if (!res.ok) {
          this._tileOnError(done, tile);
          return;
        }

        // Get image data and convert into object URL so it can be used as a src
        // Leaflet's onload listener will take it from here
        res.blob().then(blob => tile.src = URL.createObjectURL(blob));
      }).catch(() => this._tileOnError(done, tile));

    return tile;
  }
}
