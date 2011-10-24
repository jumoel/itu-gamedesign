import processing.core.*;


public class Player {
	
	public float x;
	public float y;
	public PImage texture;
	BunnyHat parent;
	private float xSpeed;
	private float ySpeed;
	private boolean isInAir;
	private boolean isJumping;
	private static float JUMPHEIGHT = 300;
	private static float JUMPSPEED = -12;
	private static float BRAKESPEED = 3f;
	private float lastGroundedY;
	private float currentXSpeed;
	
	public static float GRAVITY = 9.82f;
	public static float AIRFACTOR = 5.0f/10.0f;
	
	public Player(BunnyHat p){
		parent = p;
		x = 200;
		y = 200;
		xSpeed = 10;
		ySpeed = 0;
		currentXSpeed = 0;
		isInAir = true;
		isJumping = false;
		lastGroundedY = 0;
		texture = parent.loadImage("../player.png");
	}
	
	public void update(){
		boolean hasCollided = false;
		x += currentXSpeed;
		float sign = Math.signum(currentXSpeed);
		currentXSpeed = currentXSpeed-BRAKESPEED*sign;
		if (Math.abs(sign - Math.signum(currentXSpeed)) > 1f){
			currentXSpeed = 0;
		}
		for(Obstacle o : parent.obstacleList){
			if (o.collidesWith(this)){
				hasCollided = true;
				break;
			}
		}
		
		
		
		if (isJumping && y < (lastGroundedY-JUMPHEIGHT)){
			isJumping = false;
		}
		this.isInAir = !hasCollided;
		
		if (isJumping) {
			ySpeed = JUMPSPEED;
		}
		else if (this.isInAir){
			ySpeed = GRAVITY;
		}
		else{
			ySpeed = 0;
		}
		y+=ySpeed;
	}
	
	void jump(){
		if(!this.isInAir){
			lastGroundedY = y;
			isJumping = true;
		}
	}
	
	void draw(){
		float drawX = x-texture.width/2;
		float drawY = y-texture.height;
		parent.stroke(0);
		parent.fill(parent.color(255,0,0,255));
		parent.ellipse(x, y, 4, 4);
		parent.image(texture, drawX, drawY);

	}
	
	public void moveRight(){
		if (isInAir){
			x+= xSpeed * AIRFACTOR;
		}
		else{
			currentXSpeed = xSpeed;
		}
	}

	public void moveLeft(){
		if (isInAir){
			x-= xSpeed * AIRFACTOR;
		}
		else{
			currentXSpeed = -xSpeed;
		}
	}
	
	
	
}
