# photoNet

Android app for controlling Chitu based MSLA printers.

This software was developed and tested with the original Anycubic Photon. 
Newer Photon printers (including M3, Mono and Ultra) will not work at the moment.

IMPORTANT NOTE!

This software is capable of starting prints remotely. Please always make sure that your previous print has been removed, there is enough resin available and that the vat/build plate has been securely installed.

I advise you to monitor your printer directly, especially when starting a new print.

I am not responsible for ANY damages resulting from you using this software.

## Features

* Manage multiple printers from a single app
* Monitor the status of your printers
* Start printing files available on the storage device attached to your printer
* Upload files to your printer (these have to be pre sliced)
* Manage (list, delete, download) files stored on the storage device attached to your printer

## Requirements

* Chitu based MSLA printer with connected to your network via Ethernet or WIFI
* An Android based device running at least Android 7.0 Nougat
* A storage device connected to your printer

## Usage

### Adding a printer

Make sure your printer is switched on and connected to your local network.

In the app tap on the "+" in to lower right corner.

<img src="https://github.com/SG-O/photoNet/raw/master/doc/screenshot_empty.jpg" height="400" />
<img src="https://github.com/SG-O/photoNet/raw/master/doc/screenshot_discovery.jpg" height="400" />
<img src="https://github.com/SG-O/photoNet/raw/master/doc/screenshot_manual.jpg" height="400" />

Wait while the app discovers available printers. Tap on the printer you want to add.

If your printer was not automatically discovered tap on manual and enter your printers IP address.

### Monitor printer

Tap on your printers entry.

<img src="https://github.com/SG-O/photoNet/raw/master/doc/screenshot_main.jpg" height="400" />
<img src="https://github.com/SG-O/photoNet/raw/master/doc/screenshot_details.jpg" height="400" />

### Start print

In your printers details tap on files. Select the file you want to print. 

<img src="https://github.com/SG-O/photoNet/raw/master/doc/screenshot_files.jpg" height="400" />
<img src="https://github.com/SG-O/photoNet/raw/master/doc/screenshot_file_details.jpg" height="400" />

You will now be able to see a few details about the file you have selected. To start the print tap the play button.

### Upload Files

In your printers file list tap on the upload button in the lower right corner.

Select the file you want to upload. If you want to save the file under a different name you can do so now.
Lastly tap on upload to start the file transfer.

You can monitor the transfers progress in your notifications.

## Planned features

If you want a feature to be added to this app [feel free to create an issue](https://github.com/SG-O/photoNet/issues).

## License

[This project is licensed under the Apache-2.0 license](https://github.com/SG-O/photoNet/blob/master/LICENSE)