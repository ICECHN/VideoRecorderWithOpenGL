precision highp float;
varying highp vec2 vCamTextureCoord;
uniform sampler2D uCamTexture;


void main(){
	lowp vec4 c1 = texture2D(uCamTexture, vCamTextureCoord);

	if (vCamTextureCoord.x > 0.5)
	{
		c1 = texture2D(uCamTexture, vec2(1.0 - vCamTextureCoord.x, vCamTextureCoord.y));
	}

	gl_FragColor = c1;
}