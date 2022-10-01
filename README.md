<!-- ALL-CONTRIBUTORS-BADGE:START - Do not remove or modify this section -->
[badge-all-contributors]: https://img.shields.io/badge/all_contributors-2-orange.svg?style=flat-square
<!-- ALL-CONTRIBUTORS-BADGE:END -->

[![GitHub tag (latest SemVer)](https://img.shields.io/github/v/tag/SG-O/photoNet?label=Download)](https://github.com/SG-O/photoNet/releases/latest)
[![All Contributors][badge-all-contributors]](#contributors)
[![GitHub](https://img.shields.io/github/license/SG-O/photoNet?color=blue)](https://github.com/SG-O/photoNet/blob/master/LICENSE)
# photoNet
<p align="center">
    <img src="https://raw.githubusercontent.com/SG-O/photoNet/master/doc/appIcon.svg" height="196" />
</p>
Android app for controlling MSLA printers.
At the moment it supports the Chitu-Protocol and the Anycubic - Protocol (Mono SE, Mono X, Photon X).

This software was developed and tested with the original Anycubic Photon and Photon Mono SE. 

Newer Photon printers (including M3) will not work at the moment.

IMPORTANT NOTE!

This software is capable of starting prints remotely. Please always make sure that your previous print has been removed, there is enough resin available and that the vat/build plate has been securely installed.

I advise you to monitor your printer directly, especially when starting a new print.

I am not responsible for ANY damages resulting from you using this software.

## Features

* Manage multiple printers from a single app
* Automatic printer discovery (may not work with VPNs)
* Monitor the status of your printers
* Start printing files available on the storage device attached to your printer
* Manual movement directly from the app
* Upload files to your printer (these have to be pre sliced, only available on Chitu based printers)
* Manage (list, delete, download) files stored on the storage device attached to your printer
* Monitor your print progress through an optional IP camera (Experimental!)

## Requirements

* Chitu or Anycubic based MSLA printer with connected to your network via Ethernet or WIFI
* An Android based device running at least Android 7.0 Nougat
* A storage device connected to your printer

## Usage

### Adding a printer

Make sure your printer is switched on and connected to your local network.

In the app tap on the "+" in to lower right corner.

<img src="https://github.com/SG-O/photoNet/raw/master/doc/screenshot_empty.jpg" height="400" /> <img src="https://github.com/SG-O/photoNet/raw/master/doc/screenshot_discovery.jpg" height="400" /> <img src="https://github.com/SG-O/photoNet/raw/master/doc/screenshot_manual.jpg" height="400" />

Wait while the app discovers available printers. Tap on the printer you want to add.

If your printer was not automatically discovered tap on manual and enter your printers IP address.

### Monitor printer

Tap on your printers entry.

<img src="https://github.com/SG-O/photoNet/raw/master/doc/screenshot_main.jpg" height="400" /> <img src="https://github.com/SG-O/photoNet/raw/master/doc/screenshot_details.jpg" height="400" />

### Start print

In your printers details tap on files. Select the file you want to print. 

<img src="https://github.com/SG-O/photoNet/raw/master/doc/screenshot_files.jpg" height="400" /> <img src="https://github.com/SG-O/photoNet/raw/master/doc/screenshot_file_details.jpg" height="400" />

You will now be able to see a few details about the file you have selected. To start the print tap the play button.

### Upload Files

**This feature is only available for Chitu based printers.**

In your printers file list tap on the upload button in the lower right corner.

Select the file you want to upload. If you want to save the file under a different name you can do so now.
Lastly tap on upload to start the file transfer.

You can monitor the transfers progress in your notifications.

### IP camera

**This feature is experimental and some cameras might not be supported.**

Go to the printers settings (on the details page tap on the gear icon).

<img src="https://github.com/SG-O/photoNet/raw/master/doc/screenshot_webcam.jpg" height="400" />

Enable the webcam feature and enter your video stream's URL.

## Planned features

Planned features are [tracked here](https://github.com/users/SG-O/projects/1).

If you want a feature to be added to this app [feel free to create an issue](https://github.com/SG-O/photoNet/issues).

## Contributors

<!-- ALL-CONTRIBUTORS-LIST:START - Do not remove or modify this section -->
<!-- prettier-ignore-start -->
<!-- markdownlint-disable -->
<table>
  <tbody>
    <tr>
      <td align="center"><a href="https://github.com/SG-O"><img src="https://avatars.githubusercontent.com/u/1754351?v=4" width="100px;" alt=""/><br /><sub><b>SG-O</b></sub></a><br />
      <a href="https://github.com/SG-O/photoNet/commits?author=SG-O" title="Code">💻</a>
      <a href="#design-SG-O" title="Design">🎨</a>
       <a href="https://github.com/SG-O/photoNet/pulls?q=is%3Apr+reviewed-by%3SG-O" title="Reviewed Pull Requests">👀</a>
      <a href="#userTesting-SG-O" title="User Testing">📓</a></td>
      <td align="center"><img src="https://github.com/identicons/default.png" width="100px;" alt=""/><br /><sub><b>Brandon Stephenson</b></sub><br />
      <a href="#userTesting" title="User Testing">📓</a></td>
    </tr>
  </tbody>
</table>

<!-- markdownlint-restore -->
<!-- prettier-ignore-end -->

<!-- ALL-CONTRIBUTORS-LIST:END -->

## License

[This project is licensed under the Apache-2.0 license](https://github.com/SG-O/photoNet/blob/master/LICENSE)
