precision highp float;
varying highp vec2 vCamTextureCoord;
uniform sampler2D uCamTexture;

void main(){
	float x;
	float y;
	if (vCamTextureCoord.x <= 0.5)
	{
		x = vCamTextureCoord.x / 0.5;
		if (vCamTextureCoord.y > 0.5)
		{
			y = (vCamTextureCoord.y - 0.5) / 0.5;
		}
		else
		{
			y = (0.5 - vCamTextureCoord.y) / 0.5;
		}
	}
	else
	{
		x = (1.0 - vCamTextureCoord.x) / 0.5;
		if (vCamTextureCoord.y > 0.5)
		{
			y = (vCamTextureCoord.y - 0.5) / 0.5;
		}
		else
		{
			y = (0.5 - vCamTextureCoord.y) / 0.5;
		}
	}

	gl_FragColor = texture2D(uCamTexture, vec2(x, y));
}