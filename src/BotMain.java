import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Random;
import java.util.Scanner;

import javax.security.auth.login.LoginException;

import org.json.JSONArray;
import org.json.JSONObject;

import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.JDA;
import net.dv8tion.jda.core.JDABuilder;
import net.dv8tion.jda.core.Permission;
import net.dv8tion.jda.core.entities.ChannelType;
import net.dv8tion.jda.core.entities.Guild;
import net.dv8tion.jda.core.entities.Member;
import net.dv8tion.jda.core.entities.Message;
import net.dv8tion.jda.core.entities.MessageChannel;
import net.dv8tion.jda.core.entities.TextChannel;
import net.dv8tion.jda.core.entities.User;
import net.dv8tion.jda.core.events.message.MessageReceivedEvent;
import net.dv8tion.jda.core.exceptions.PermissionException;
import net.dv8tion.jda.core.hooks.ListenerAdapter;

public class BotMain extends ListenerAdapter
{
	
	public String lastGrabBagCode;
	public static UserData userData;
	
    public static void main(String[] args)
    {
        //We construct a builder for a BOT account. If we wanted to use a CLIENT account
        // we would use AccountType.CLIENT
        try
        {
            JDA jda = new JDABuilder(LoadStringFromFile("botToken"))         // The token of the account that is logging in.
                    .addEventListener(new BotMain())  // An instance of a class that will handle events.
                    .build();
            jda.awaitReady(); // Blocking guarantees that JDA will be completely loaded.
            System.out.println("Finished Building JDA!");
        }
        catch (LoginException e)
        {
            //If anything goes wrong in terms of authentication, this is the exception that will represent it
            e.printStackTrace();
        }
        catch (InterruptedException e)
        {
            //Due to the fact that awaitReady is a blocking method, one which waits until JDA is fully loaded,
            // the waiting can be interrupted. This is the exception that would fire in that situation.
            //As a note: in this extremely simplified example this will never occur. In fact, this will never occur unless
            // you use awaitReady in a thread that has the possibility of being interrupted (async thread usage and interrupts)
            e.printStackTrace();
        }
        
        userData = new UserData();
        userData.LoadData("userData");
    }
    
    @Override
    public void onMessageReceived(MessageReceivedEvent event)
    {
        //These are provided with every event in JDA
        JDA jda = event.getJDA();                       //JDA, the core of the api.
        long responseNumber = event.getResponseNumber();//The amount of discord events that JDA has received since the last reconnect.

        //Event specific information
        User author = event.getAuthor();                //The user that sent the message
        Message message = event.getMessage();           //The message that was received.
        MessageChannel channel = event.getChannel();    //This is the MessageChannel that the message was sent to.
                                                        //  This could be a TextChannel, PrivateChannel, or Group!

        String msg = message.getContentDisplay();              //This returns a human readable version of the Message. Similar to
                                                        // what you would see in the client.

        boolean bot = author.isBot();                    //This boolean is useful to determine if the User that
                                                        // sent the Message is a BOT or not!

        if (event.isFromType(ChannelType.TEXT))         //If this message was sent to a Guild TextChannel
        {
            //Because we now know that this message was sent in a Guild, we can do guild specific things
            // Note, if you don't check the ChannelType before using these methods, they might return null due
            // the message possibly not being from a Guild!

            Guild guild = event.getGuild();             //The Guild that this message was sent in. (note, in the API, Guilds are Servers)
            TextChannel textChannel = event.getTextChannel(); //The TextChannel that this message was sent to.
            Member member = event.getMember();          //This Member that sent the message. Contains Guild specific information about the User!

            String name;
            if (message.isWebhookMessage())
            {
                name = author.getName();                //If this is a Webhook message, then there is no Member associated
            }                                           // with the User, thus we default to the author for name.
            else
            {
                name = member.getEffectiveName();       //This will either use the Member's nickname if they have one,
            }                                           // otherwise it will default to their username. (User#getName())

            System.out.printf("(%s)[%s]<%s>: %s\n", guild.getName(), textChannel.getName(), name, msg);
            
            if(userData.UserExists(author.getId())) {
            	
            }else {
            	channel.sendMessage("Hey " + name + ", welcome to " + guild.getName() + "!").queue();
            	userData.AddUser(author.getId());
            	userData.SetUserVariable(author.getId(), "chatMessages", "0");
            }
        }
        
        userData.SetUserVariable(author.getId(), "screenName", author.getName());
        userData.SetUserVariable(author.getId(), "chatMessages", Integer.toString(Integer.parseInt(userData.GetUserVariable(author.getId(), "chatMessages"))+1));
        
        userData.SaveData("userData");
        
        if (msg.equals("!ping"))
        {
            //This will send a message, "pong!", by constructing a RestAction and "queueing" the action with the Requester.
            // By calling queue(), we send the Request to the Requester which will send it to discord. Using queue() or any
            // of its different forms will handle ratelimiting for you automatically!

            channel.sendMessage("pong!").queue();
        }
        else if(msg.contains("!roll")) {
        	String temp = msg.substring(msg.indexOf("!roll") + 5).trim();
        	int tempInt = 0;
        	try {
        		tempInt = Integer.parseInt(temp);
        		
        		Random rand = new Random();
        		int roll = rand.nextInt(tempInt) + 1; //This results in 1 - 6 (instead of 0 - 5)
                
                channel.sendMessage("Your roll: " + roll).queue();
			} catch (Exception e) {
				channel.sendMessage("Please enter a **real** number.").queue();
			}
            
        }
        else if(msg.equals("!burn")){
        	channel.sendMessage("*o u c h*").queue();
        }
        else if(msg.equals("!but")){
        	channel.sendMessage("hey that's just a theory, a game theory").queue();
        }
        else if(msg.equals("!points")){
        	channel.sendMessage("You have " + userData.GetUserVariable(author.getId(), "chatMessages") + " points.").queue();
        }
        else if(msg.equals("!joke")) {
        	DoJokeCommand(msg, channel);
        }
        else if(msg.contains("!cat")) {
        	DoCatCommand(msg, channel);
        }
        else if(msg.contains("!dog")) {
        	DoDogCommand(msg, channel);
        }
        else if(msg.equals("!meme")){
        	EmbedBuilder eb = new EmbedBuilder();
        	eb.setImage(GetMemeUrl("r/dankmemes"));
        	channel.sendMessage(eb.build()).queue();
        }
        else if(msg.equals("!grabbag")) {
        	if(IsGrabBagChanged()) {
        		channel.sendMessage("Yeah, I think they are in stock now! Here check it out: https://pimpmykeyboard.com/grab-bags-domestic-us-shipping/").queue();
        	}else {
        		channel.sendMessage("Nope, I don't think they are in stock yet.").queue();
        	}
        }
        else if(msg.equals("!laser")) {
        	EmbedBuilder eb = new EmbedBuilder();
        	eb.setImage("https://cdn2.thecatapi.com/images/3oi.gif");
        	channel.sendMessage(eb.build()).queue();
        }
        else if(msg.equals("!lasers")) {
        	EmbedBuilder eb = new EmbedBuilder();
        	eb.setImage("https://cdn2.thecatapi.com/images/3oi.gif");
        	channel.sendMessage(eb.build()).queue();
        	channel.sendMessage(eb.build()).queue();
        }
        else if (msg.startsWith("!kick"))   //Note, I used "startsWith, not equals.
        {
            DoKickCommand(message, event, msg, channel);
        }
    }
    
    private static void DoJokeCommand(String msg, MessageChannel channel) {
    	String joke = GetJoke();
    	
    	if(joke != null) {
    		channel.sendMessage(joke).queue();
    	}else {
    		channel.sendMessage("oof, something went wrong").queue();
    	}
    }
    
    private static void DoKickCommand(Message message, MessageReceivedEvent event, String msg, MessageChannel channel) {
    	//This is an admin command. That means that it requires specific permissions to use it, in this case
        // it needs Permission.KICK_MEMBERS. We will have a check before we attempt to kick members to see
        // if the logged in account actually has the permission, but considering something could change after our
        // check we should also take into account the possibility that we don't have permission anymore, thus Discord
        // response with a permission failure!
        //We will use the error consumer, the second parameter in queue!

        //We only want to deal with message sent in a Guild.
        if (message.isFromType(ChannelType.TEXT))
        {
            //If no users are provided, we can't kick anyone!
            if (message.getMentionedUsers().isEmpty())
            {
                channel.sendMessage("You must mention 1 or more Users to be kicked!").queue();
            }
            else
            {
                Guild guild = event.getGuild();
                Member selfMember = guild.getSelfMember();  //This is the currently logged in account's Member object.
                                                            // Very similar to JDA#getSelfUser()!

                //Now, we the the logged in account doesn't have permission to kick members.. well.. we can't kick!
                if (!selfMember.hasPermission(Permission.KICK_MEMBERS))
                {
                    channel.sendMessage("Sorry! I don't have permission to kick members in this Guild!").queue();
                    return; //We jump out of the method instead of using cascading if/else
                }

                //Loop over all mentioned users, kicking them one at a time. Mwauahahah!
                List<User> mentionedUsers = message.getMentionedUsers();
                for (User user : mentionedUsers)
                {
                    Member member = guild.getMember(user);  //We get the member object for each mentioned user to kick them!

                    //We need to make sure that we can interact with them. Interacting with a Member means you are higher
                    // in the Role hierarchy than they are. Remember, NO ONE is above the Guild's Owner. (Guild#getOwner())
                    if (!selfMember.canInteract(member))
                    {
                        // use the MessageAction to construct the content in StringBuilder syntax using append calls
                        channel.sendMessage("Cannot kick member: ")
                               .append(member.getEffectiveName())
                               .append(", they are higher in the hierarchy than I am!")
                               .queue();
                        continue;   //Continue to the next mentioned user to be kicked.
                    }

                    //Remember, due to the fact that we're using queue we will never have to deal with RateLimits.
                    // JDA will do it all for you so long as you are using queue!
                    guild.getController().kick(member).queue(
                        success -> channel.sendMessage("Kicked ").append(member.getEffectiveName()).append("! Cya!").queue(),
                        error ->
                        {
                            //The failure consumer provides a throwable. In this case we want to check for a PermissionException.
                            if (error instanceof PermissionException)
                            {
                                PermissionException pe = (PermissionException) error;
                                Permission missingPermission = pe.getPermission();  //If you want to know exactly what permission is missing, this is how.
                                                                                    //Note: some PermissionExceptions have no permission provided, only an error message!

                                channel.sendMessage("PermissionError kicking [")
                                       .append(member.getEffectiveName()).append("]: ")
                                       .append(error.getMessage()).queue();
                            }
                            else
                            {
                                channel.sendMessage("Unknown error while kicking [")
                                       .append(member.getEffectiveName())
                                       .append("]: <").append(error.getClass().getSimpleName()).append(">: ")
                                       .append(error.getMessage()).queue();
                            }
                        });
                }
            }
        }
        else
        {
            channel.sendMessage("This is a Guild-Only command!").queue();
        }
    }
    
    private static void DoCatCommand(String msg, MessageChannel channel) {
    	int numberOfCats = 1;
    	if(msg.length() == 4) {
    		numberOfCats = 1;
    	}else {
    		try {
    			String temp = msg.substring(msg.indexOf("!cat") + 4).trim();
        		numberOfCats = Integer.parseInt(temp);
			} catch (Exception e) {
				numberOfCats = 0;
			}
    	}
    	if(numberOfCats <= 10) {
        	for(int i=0;i<numberOfCats;i++) {
        		String catUrl = GetCatImageUrl();
            	
            	if(catUrl != null) {
            		EmbedBuilder eb = new EmbedBuilder();
                	eb.setImage(catUrl);
                	channel.sendMessage(eb.build()).queue();
            	}else {
            		channel.sendMessage("oof, something went wrong").queue();
            	}
        	}
    	}else {
    		channel.sendMessage("Yeah, that's too many cats..").queue();
    	}
    }
    
    private static void DoDogCommand(String msg, MessageChannel channel) {
    	int numberOfDogs = 1;
    	if(msg.length() == 4) {
    		numberOfDogs = 1;
    	}else {
    		try {
    			String temp = msg.substring(msg.indexOf("!dog") + 4).trim();
    			numberOfDogs = Integer.parseInt(temp);
			} catch (Exception e) {
				numberOfDogs = 0;
			}
    	}
    	if(numberOfDogs <= 10) {
        	for(int i=0;i<numberOfDogs;i++) {
        		String dogUrl = GetDogImageUrl();
            	
            	if(dogUrl != null) {
            		EmbedBuilder eb = new EmbedBuilder();
                	eb.setImage(dogUrl);
                	channel.sendMessage(eb.build()).queue();
            	}else {
            		channel.sendMessage("oof, something went wrong").queue();
            	}
        	}
    	}else {
    		channel.sendMessage("Yeah, that's too many dogs..").queue();
    	}
    }
    
    private static String GetJoke() {

    	String url = "https://icanhazdadjoke.com/";
    	
		try {
			String unparsedResponse = GetHttpData(url);
			
			unparsedResponse = unparsedResponse.substring(unparsedResponse.indexOf("og:description")+25);
	    	unparsedResponse = unparsedResponse.substring(0,unparsedResponse.indexOf("/><meta property=")-2);
	    	unparsedResponse = unparsedResponse.replaceAll("’", "\'");
	    	//return result
	    	return unparsedResponse;
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			
			return null;
		}
    }
    
    public boolean IsGrabBagChanged() {
		try {
			String val = GetHttpData("https://pimpmykeyboard.com/grab-bags-domestic-us-shipping/");
			if(lastGrabBagCode.equals(val)) {
				lastGrabBagCode = val;
				return false;
			}else {
				lastGrabBagCode = val;
				return true;
			}
		} catch (Exception e) {
			return false;
		}
    }
    
    private static String GetCatImageUrl() {

		String url = "https://api.thecatapi.com/v1/images/search";
		
		try {
			String unparsedResponse = GetHttpData(url);
			
			unparsedResponse = unparsedResponse.substring(unparsedResponse.indexOf("url")+6, unparsedResponse.length()-3);
			//return result
			return unparsedResponse;
		} catch (Exception e) {
			
			e.printStackTrace();
			
			return "null";
		}
	}
    
    private static String GetDogImageUrl() {

		String url = "https://api.thedogapi.com/v1/images/search";
		
		try {
			String unparsedResponse = GetHttpData(url);
			
			unparsedResponse = unparsedResponse.substring(unparsedResponse.indexOf("url")+6, unparsedResponse.length()-3);
			//return result
			return unparsedResponse;
		} catch (Exception e) {
			
			e.printStackTrace();
			
			return "null";
		}
	}
    
    private static String GetMemeUrl(String subreddit) {

		String url = "https://www.reddit.com/"+subreddit+".json?sort=top&t=week";
		
		try {
			String unparsedResponse = GetHttpData(url);
			
			JSONObject obj = new JSONObject(unparsedResponse);
			
			JSONArray objData = obj.getJSONObject("data").getJSONArray("children");
			
			Random random = new Random();
			
			return objData.getJSONObject(random.nextInt(objData.length())).getJSONObject("data").getString("url");
		} catch (Exception e) {
			
			e.printStackTrace();
			
			return "null";
		}
	}
    
    private static String GetHttpData(String url) throws Exception {
    	URL obj = new URL(url);
		HttpURLConnection con = (HttpURLConnection) obj.openConnection();

		// optional default is GET
		con.setRequestMethod("GET");

		//add request header
		con.setRequestProperty("User-Agent", "Mozilla/5.0");

		int responseCode = con.getResponseCode();

		BufferedReader in = new BufferedReader(
		        new InputStreamReader(con.getInputStream()));
		String inputLine;
		StringBuffer response = new StringBuffer();

		while ((inputLine = in.readLine()) != null) {
			response.append(inputLine);
		}
		in.close();
		
		return response.toString();
    }
    
    public static String LoadStringFromFile(String fileName) {
		File file = new File(fileName + ".txt");
		Scanner sc;
		try {
			sc = new Scanner(file);
		} catch (FileNotFoundException e) {
			// TODO Auto-generated catch block
			System.out.println("File \"" + fileName + ".txt\" not found...");
			return null;
		}
		String line = sc.nextLine();
		sc.close();
		return line;
	}
}
