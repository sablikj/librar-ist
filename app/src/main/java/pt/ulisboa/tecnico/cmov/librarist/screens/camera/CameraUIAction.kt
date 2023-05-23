package pt.ulisboa.tecnico.cmov.librarist.screens.camera

sealed class CameraUIAction {
    object OnCameraClick : CameraUIAction()
    object OnDenyImageClick : CameraUIAction()
    object OnAcceptImageClick : CameraUIAction()
}