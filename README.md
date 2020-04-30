# BayEOS Logger FX
A utility to communicate with BayEOS logger devices 

Main characteristics:
- USB or Bluetooth communication
- Logger configuration
- Data download
- Data upload to a [BayEOS Gateway](http://https://github.com/BayCEER/bayeos-gateway)
- Live observation mode 

Please refer to the ![user guide](/docs/user-guide.md) for more information. 

## Authors 
* **Dr. Stefan Holzheu** - *Project lead* - [BayCEER, University of Bayreuth](https://www.bayceer.uni-bayreuth.de)
* **Oliver Archner** - *Programmer* - [BayCEER, University of Bayreuth](https://www.bayceer.uni-bayreuth.de)

## History
### Version 2.0.11, 2020
- Fixed critical bug: Errors on upload not detected

### Version 2.0.10,  2020
- Reduced serial connection timeout from 10 to 2 seconds
- Enhancement: Out of order support in bulk writer
- Sampling interval renamed to logging interval
- Bundled JRE for Windows  
 
### Version 2.0.9,  2020
- Enhancement: Render all dates in system time zone
- Enhancement: Info generation on corrupt files 


### Version 2.0.8, September 19, 2018
- Fixed bug: GetBattery status  

### Version 2.0.7, July 30, 2018
- Fixed bug: [Progress bar](https://github.com/BayCEER/bayeos-logger-fx/issues/5)

### Version 2.0.6, July 10, 2018
- Fixed bug: Save incomplete data frames to file 

### Version 2.0.5, May 15, 2018
- WebStart support dropped
- Fixed bug: Delete logger data 

## License
GNU LESSER GENERAL PUBLIC LICENSE, Version 2.1, February 1999