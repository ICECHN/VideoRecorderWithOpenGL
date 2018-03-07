#ifdef SPLIT_SQUARE_BASE
#define NUM SPLIT_SQUARE_BASE
#else
#define NUM 2.
#endif

precision highp float;
varying highp vec2 vCamTextureCoord;
uniform sampler2D uCamTexture;

void main(){
	float subLen = 1.0 / NUM;

    float xIdx = floor(vCamTextureCoord.x / subLen);
	float yIdx = floor(vCamTextureCoord.y / subLen);

	float rLeft = xIdx * subLen;
	float rBottom = yIdx * subLen;

	float x = (vCamTextureCoord.x - rLeft) / subLen;
	float y = (vCamTextureCoord.y - rBottom) / subLen;

	gl_FragColor = texture2D(uCamTexture, vec2(x, y));
}