attribute vec4 aCamPosition;
attribute vec2 aCamTextureCoord;
varying vec2 vCamTextureCoord;
void main(){
    gl_Position= aCamPosition;
    vCamTextureCoord = aCamTextureCoord;
}