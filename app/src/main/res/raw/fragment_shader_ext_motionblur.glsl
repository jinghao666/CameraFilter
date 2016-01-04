#extension GL_OES_EGL_image_external : require
precision highp float;

varying highp vec2 vTextureCoord;
uniform samplerExternalOES uTexture;

uniform vec2 samplerStep;
uniform vec2 blurNorm;
uniform float samplerRadius;
const int number = 3;//10; // sum = number*2 + 1

float random(vec2 seed)
{
	return fract(sin(dot(seed ,vec2(12.9898,78.233))) * 43758.5453);
}

void main()
{
    vec4 src = texture2D(uTexture, vTextureCoord);
    // 	if(samplerRadius == 0.0)
    // 	{
    // 		gl_FragColor = vec4(src, 0.0);
    // 		return;
    // 	}
	vec4 resultColor = vec4(0.0);
	float blurPixels = 0.0;
	float offset = random(vTextureCoord) - 0.5;

	vec2 arg = blurNorm * samplerStep * samplerRadius;

	for(int i = -number; i <= number; ++i)
	{
		float percent = (float(i) + offset) / float(number);
		float weight = 1.0 - abs(percent);
		vec2 coord = vTextureCoord + percent * arg;
		resultColor += texture2D(uTexture, coord) * weight;
		blurPixels += weight;
	}
	gl_FragColor = resultColor / blurPixels;
}