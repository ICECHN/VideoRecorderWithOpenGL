#ifdef GAUSSIAN_BLUR_RADIUS
#define RADIUS GAUSSIAN_BLUR_RADIUS
#else
#define RADIUS 10.
#endif

precision highp float;
varying vec2 vCamTextureCoord;
uniform sampler2D uCamTexture;
uniform vec2 step;
uniform vec4 ignoreRect;
const int blurRadius = 10;

void main(){
	lowp vec2 point = vec2(vCamTextureCoord.x,vCamTextureCoord.y);
	if(ignoreRect.r < ignoreRect.b && ignoreRect.g < ignoreRect.a
		&& point.x>ignoreRect.r && point.x<ignoreRect.b
		&& point.y>ignoreRect.g && point.y<ignoreRect.a)
	{
		gl_FragColor = texture2D(uCamTexture, vCamTextureCoord);
		return ;
	}

	vec2 currPos = vCamTextureCoord - (step*RADIUS);
    vec2 endPos = vCamTextureCoord + (step*RADIUS);
	vec3 sum=vec3(0.,0.,0.);
    float fact=0.;
    for(float r=-RADIUS;r<=RADIUS;r+=1.)
    {
	    if(any(lessThanEqual(currPos,vec2(0.,0.)))||any(greaterThanEqual(currPos,vec2(1.,1.))))
        {
			currPos+=step;
            continue;
        }
        float gauss = exp(r*r / (-4.*RADIUS));
        sum += pow(texture2D(uCamTexture,currPos).rgb,vec3(2.,2.,2.))*gauss;
        fact += gauss;
        currPos+=step;
    }
    gl_FragColor = vec4(sqrt(sum/fact),1.);
}