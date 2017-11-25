precision highp float;
varying highp vec2 vCamTextureCoord;
uniform sampler2D uCamTexture;
uniform sampler2D uImageTexture;
uniform vec4 imageRect;
uniform float imageAngel;

vec2 rotate(vec2 p0, vec2 center, float angel)
{
	float x2 = (p0.x - center.x)*cos(angel) - (p0.y - center.y)*sin(angel) + center.x ;
	float y2 = (p0.x - center.x)*sin(angel) + (p0.y - center.y)*cos(angel) + center.y ;
	return vec2(x2, y2);
}
void main(){
	lowp vec4 c1 = texture2D(uCamTexture, vCamTextureCoord);
	lowp vec2 vCamTextureCoord2 = vec2(vCamTextureCoord.x,1.0-vCamTextureCoord.y);
	vec2 point = vCamTextureCoord2;
	if(imageAngel != 0.0)
	{
		vec2 center = vec2((imageRect.r+imageRect.b)/2.0, (imageRect.g+imageRect.a)/2.0);
		vec2 p2 = rotate(vCamTextureCoord2, center, -imageAngel);
		point = p2;
	}
		if(point.x>imageRect.r && point.x<imageRect.b && point.y>imageRect.g && point.y<imageRect.a)
		{
			vec2 imagexy = vec2((point.x-imageRect.r)/(imageRect.b-imageRect.r),(point.y-imageRect.g)/(imageRect.a-imageRect.g));
			lowp vec4 c2 = texture2D(uImageTexture, imagexy);
			lowp vec4 outputColor = c2+c1*c1.a*(1.0-c2.a);
			outputColor.a = 1.0;
			gl_FragColor = outputColor;
		}else{
			gl_FragColor = c1;
		}
}