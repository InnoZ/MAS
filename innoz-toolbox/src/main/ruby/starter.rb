class Starter
	
	DEFAULT = './output/'
	
	def initialize (id, year, folder = DEFAULT)
	  
    unless year.between?(2009,2040)
      fail 'Scenario generation only works for years between 2009 and 2040 at the moment'
    end
    
	  @id = id
		@year = year
		@folder = folder
	
	end

	def start
		Kernel.exec "java -cp /home/dhosse/workspace/MAS/innoz-toolbox/target/innoz-toolbox-0.1-SNAPSHOT.jar com.innoz.toolbox.run.Main #{id} #{year} #{folder}"
	end

	private
	attr_reader :id, :year, :folder

end

Starter.new(15001,2017).start