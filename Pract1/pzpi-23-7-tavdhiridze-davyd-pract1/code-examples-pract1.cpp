//class Light {
//	 turnOn() { console.log("Light is ON"); }
//}
// class Button {
//	 constructor(light) {
//		this.light = light;
//		
//	}
//	 press() { this.light.turnOn(); }
//	
//}
// const light = new Light();
// const button = new Button(light); button.press(); // Light is ON 

 //interface Command { execute() : void; }
 // class Light {
	//  turnOn() { console.log("Light is ON"); }
	// 
 //}
 // class TurnOnLightCommand implements Command {
	// constructor(light) {
	//	 this.light  = light;
	//	 
	// }
	//  execute() { this.light.turnOn(); }
	// 
 //}
 // class Switch {
	//  constructor(command) { this.command = command; }
	//  press() { this.command.execute(); }
	// 
 //}
 // const light = new Light();
 // const turnOnCommand = new TurnOnLightCommand(light);
 // const _switch = new Switch(turnOnCommand);
 // _switch.press(); // Light is ON 

  //class Command {
  // public:
	 //  virtual void execute() = 0;
	 //  virtual ~Command() {}
	 // 
  //};
  // class Light {
  // public:
	 //  void turnOn() {
		//   std::cout << "Light is ON\n";
		//  
	 // }
	 //  void turnOff() {
		//    std::cout << "Light is OFF\n";
		//  
	 // }
	 // 
  //};

   class LightOnCommand : public Command {
	   Light* light;
    public:
	    LightOnCommand(Light * l) : light(l) {}
	    void execute() override {
		    light->turnOn();
		   
	   }
	   
   };
    class RemoteControl {
	   Command * command;
    public:
	    void setCommand(Command * cmd) {
		    command = cmd;
		   
	   }
	    void pressButton() {
		    command->execute();
		   
	   }
	   
   };
